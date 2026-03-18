package app.domain.services.implementations;

import app.domain.enums.AccountStatus;
import app.domain.enums.SystemRole;
import app.domain.enums.UserStatus;
import app.domain.models.BankAccount;
import app.domain.models.User;
import app.domain.services.interfaces.IAccountService;
import app.domain.services.interfaces.IAuthService;
import app.domain.services.interfaces.ILogService;
import app.domain.services.interfaces.IUserService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class AccountService implements IAccountService {

    private final List<BankAccount> accounts = new ArrayList<>();
    private final IAuthService authService;
    private final IUserService userService;
    private final ILogService logService;

    public AccountService(IAuthService authService, IUserService userService, ILogService logService) {
        this.authService = authService;
        this.userService = userService;
        this.logService = logService;
    }

    // Acceso directo a la lista para otros servicios (ej: TransferService necesita modificar saldos)
    public List<BankAccount> getAccountsStorage() {
        return accounts;
    }

    @Override
    public BankAccount openAccount(BankAccount account, User requestingUser) {
        // Regla de acceso: Empleado de Ventanilla o Empleado Comercial
        authService.validateRole(requestingUser,
                SystemRole.TELLER_EMPLOYEE, SystemRole.COMMERCIAL_EMPLOYEE);

        // Validar que el titular exista y esté activo
        User holder = userService.findByIdentificationId(account.getAccountHolderId());
        if (holder.getUserStatus() == UserStatus.INACTIVE || holder.getUserStatus() == UserStatus.BLOCKED) {
            throw new IllegalStateException(
                "No se puede abrir una cuenta: el cliente está " + holder.getUserStatus() + ".");
        }

        // Regla de negocio: Número de cuenta único
        if (account.getAccountNumber() == null || account.getAccountNumber().isBlank()) {
            throw new IllegalArgumentException("El número de cuenta es obligatorio.");
        }
        boolean accountExists = accounts.stream()
                .anyMatch(a -> a.getAccountNumber().equals(account.getAccountNumber()));
        if (accountExists) {
            throw new IllegalArgumentException(
                "Ya existe una cuenta con el número: " + account.getAccountNumber());
        }

        // Validar tipo de cuenta
        if (account.getAccountType() == null) {
            throw new IllegalArgumentException("El tipo de cuenta es obligatorio.");
        }

        // Saldo inicial no puede ser negativo
        if (account.getBalance() < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo.");
        }

        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setOpeningDate(LocalDate.now().toString());
        accounts.add(account);

        // Registrar en bitácora
        Map<String, Object> detail = new HashMap<>();
        detail.put("accountNumber", account.getAccountNumber());
        detail.put("accountType", account.getAccountType().toString());
        detail.put("holderId", account.getAccountHolderId());
        detail.put("initialBalance", account.getBalance());
        logService.log("ACCOUNT_OPENED", requestingUser, account.getAccountNumber(), detail);

        System.out.println("Cuenta abierta: " + account.getAccountNumber() + " para " + account.getAccountHolderId());
        return account;
    }

    @Override
    public BankAccount findByAccountNumber(String accountNumber, User requestingUser) {
        BankAccount account = accounts.stream()
                .filter(a -> a.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "No se encontró la cuenta: " + accountNumber));

        // Restricción de acceso por rol
        checkAccountVisibility(account, requestingUser);
        return account;
    }

    @Override
    public List<BankAccount> getAccountsByHolder(String holderId, User requestingUser) {
        // Los clientes solo pueden ver sus propias cuentas
        if (requestingUser.getSystemRole() == SystemRole.INDIVIDUAL_CUSTOMER ||
            requestingUser.getSystemRole() == SystemRole.CORPORATE_CUSTOMER ||
            requestingUser.getSystemRole() == SystemRole.CORPORATE_EMPLOYEE) {
            if (!requestingUser.getIdentificationId().equals(holderId) &&
                !requestingUser.getRelatedid().equals(holderId)) {
                throw new SecurityException("No tiene permiso para ver las cuentas de otro cliente.");
            }
        }
        return accounts.stream()
                .filter(a -> a.getAccountHolderId().equals(holderId))
                .collect(Collectors.toList());
    }

    @Override
    public void deposit(String accountNumber, double amount, User requestingUser) {
        authService.validateRole(requestingUser, SystemRole.TELLER_EMPLOYEE);

        if (amount <= 0) throw new IllegalArgumentException("El monto del depósito debe ser mayor a cero.");

        BankAccount account = getActiveAccount(accountNumber);
        double balanceBefore = account.getBalance();
        account.setBalance(account.getBalance() + amount);

        Map<String, Object> detail = new HashMap<>();
        detail.put("amount", amount);
        detail.put("balanceBefore", balanceBefore);
        detail.put("balanceAfter", account.getBalance());
        logService.log("DEPOSIT", requestingUser, accountNumber, detail);

        System.out.printf("Depósito de %.2f en cuenta %s. Nuevo saldo: %.2f%n",
                amount, accountNumber, account.getBalance());
    }

    @Override
    public void withdraw(String accountNumber, double amount, User requestingUser) {
        authService.validateRole(requestingUser, SystemRole.TELLER_EMPLOYEE);

        if (amount <= 0) throw new IllegalArgumentException("El monto del retiro debe ser mayor a cero.");

        BankAccount account = getActiveAccount(accountNumber);

        if (account.getBalance() < amount) {
            throw new IllegalStateException(
                "Saldo insuficiente. Disponible: " + account.getBalance() + ", solicitado: " + amount);
        }

        double balanceBefore = account.getBalance();
        account.setBalance(account.getBalance() - amount);

        Map<String, Object> detail = new HashMap<>();
        detail.put("amount", amount);
        detail.put("balanceBefore", balanceBefore);
        detail.put("balanceAfter", account.getBalance());
        logService.log("WITHDRAWAL", requestingUser, accountNumber, detail);

        System.out.printf("Retiro de %.2f de cuenta %s. Nuevo saldo: %.2f%n",
                amount, accountNumber, account.getBalance());
    }

    // ──────────────────────────────────────────────
    // Métodos de utilidad (usados por otros servicios)
    // ──────────────────────────────────────────────

    /**
     * Obtiene una cuenta activa. Lanza excepción si no existe o no está activa.
     * Usado internamente por TransferService y LoanService.
     */
    public BankAccount getActiveAccount(String accountNumber) {
        BankAccount account = accounts.stream()
                .filter(a -> a.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + accountNumber));

        if (account.getAccountStatus() == AccountStatus.BLOCKED ||
            account.getAccountStatus() == AccountStatus.CANCELLED) {
            throw new IllegalStateException(
                "La cuenta " + accountNumber + " está " + account.getAccountStatus() +
                " y no puede realizar operaciones.");
        }
        return account;
    }

    // ──────────────────────────────────────────────
    // Métodos privados
    // ──────────────────────────────────────────────

    private void checkAccountVisibility(BankAccount account, User requestingUser) {
        SystemRole role = requestingUser.getSystemRole();
        // Clientes solo ven sus propias cuentas
        if (role == SystemRole.INDIVIDUAL_CUSTOMER || role == SystemRole.CORPORATE_CUSTOMER) {
            if (!account.getAccountHolderId().equals(requestingUser.getIdentificationId())) {
                throw new SecurityException("No tiene permiso para ver esta cuenta.");
            }
        }
        // Empleado de Empresa solo ve cuentas de su empresa
        if (role == SystemRole.CORPORATE_EMPLOYEE || role == SystemRole.CORPORATE_SUPERVISOR) {
            if (!account.getAccountHolderId().equals(requestingUser.getRelatedid())) {
                throw new SecurityException("No tiene permiso para ver las cuentas de otra empresa.");
            }
        }
        // Teller, Comercial, Analista → acceso amplio (ya validado por rol en el método público)
    }
}

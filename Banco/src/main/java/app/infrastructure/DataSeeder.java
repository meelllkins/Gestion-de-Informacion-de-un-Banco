package app.infrastructure;

import app.application.usecases.CommercialEmployeeUseCase;
import app.application.usecases.CorporateCustomerUseCase;
import app.application.usecases.CorporateEmployeeUseCase;
import app.application.usecases.CorporateSupervisorUseCase;
import app.application.usecases.IndividualCustomerUseCase;
import app.application.usecases.InternalAnalystUseCase;
import app.application.usecases.TellerEmployeeUseCase;
import app.domain.models.BankAccount;
import app.domain.models.CorporateCustomer;
import app.domain.models.IndividualCustomer;
import app.domain.models.User;
import app.domain.models.enums.AccountType;
import app.domain.models.enums.Category;
import app.domain.models.enums.Currency;
import app.domain.ports.IAccountPort;
import app.domain.ports.IUserPort;
import app.domain.services.implementations.OpenAccount;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements ApplicationRunner {

	private static final String ANALYST_ID = "8000000001";
	private static final String ANALYST_USERNAME = "analyst_user";
	private static final String ANALYST_PASSWORD = "analyst_password";

	private static final String CORPORATE_ID = "9000000001";
	private static final String CORPORATE_USERNAME = "seed_corp_admin";
	private static final String CORPORATE_PASSWORD = "SeedPass123!";

	private static final String CORPORATE_EMPLOYEE_ID = "9000000002";
	private static final String CORPORATE_EMPLOYEE_USERNAME = "seed_corp_emp";

	private static final String CORPORATE_SUPERVISOR_ID = "9000000003";
	private static final String CORPORATE_SUPERVISOR_USERNAME = "seed_corp_sup";

	private static final String COMMERCIAL_EMPLOYEE_ID = "9000000004";
	private static final String COMMERCIAL_EMPLOYEE_USERNAME = "seed_commercial";

	private static final String TELLER_EMPLOYEE_ID = "9000000005";
	private static final String TELLER_EMPLOYEE_USERNAME = "seed_teller";

	private static final String INDIVIDUAL_ID = "9000000006";
	private static final String INDIVIDUAL_USERNAME = "seed_individual";

	private static final String ACCOUNT_INDIVIDUAL = "ACC-10001";
	private static final String ACCOUNT_CORPORATE = "ACC-20001";

	private final InternalAnalystUseCase internalAnalystUseCase;
	private final CorporateCustomerUseCase corporateCustomerUseCase;
	private final CorporateEmployeeUseCase corporateEmployeeUseCase;
	private final CorporateSupervisorUseCase corporateSupervisorUseCase;
	private final CommercialEmployeeUseCase commercialEmployeeUseCase;
	private final TellerEmployeeUseCase tellerEmployeeUseCase;
	private final IndividualCustomerUseCase individualCustomerUseCase;
	private final OpenAccount openAccount;
	private final IUserPort userPort;
	private final IAccountPort accountPort;

	public DataSeeder(InternalAnalystUseCase internalAnalystUseCase,
					  CorporateCustomerUseCase corporateCustomerUseCase,
					  CorporateEmployeeUseCase corporateEmployeeUseCase,
					  CorporateSupervisorUseCase corporateSupervisorUseCase,
					  CommercialEmployeeUseCase commercialEmployeeUseCase,
					  TellerEmployeeUseCase tellerEmployeeUseCase,
					  IndividualCustomerUseCase individualCustomerUseCase,
					  OpenAccount openAccount,
					  IUserPort userPort,
					  IAccountPort accountPort) {
		this.internalAnalystUseCase = internalAnalystUseCase;
		this.corporateCustomerUseCase = corporateCustomerUseCase;
		this.corporateEmployeeUseCase = corporateEmployeeUseCase;
		this.corporateSupervisorUseCase = corporateSupervisorUseCase;
		this.commercialEmployeeUseCase = commercialEmployeeUseCase;
		this.tellerEmployeeUseCase = tellerEmployeeUseCase;
		this.individualCustomerUseCase = individualCustomerUseCase;
		this.openAccount = openAccount;
		this.userPort = userPort;
		this.accountPort = accountPort;
	}

	@Override
	public void run(ApplicationArguments args) {
		seedInternalAnalyst();
		seedCorporateCustomer();
		seedCorporateEmployee();
		seedCorporateSupervisor();
		User commercial = seedCommercialEmployee();
		User teller = seedTellerEmployee();
		seedIndividualCustomer();
		seedAccounts(teller, commercial);
	}

	private User seedInternalAnalyst() {
		return userPort.findByIdentificationId(ANALYST_ID)
				.orElseGet(() -> {
					User analyst = new User();
					analyst.setName("Laura Castillo");
					analyst.setIdentificationId(ANALYST_ID);
					analyst.setEmail("analyst@bank.test");
					analyst.setPhone("3000000001");
					analyst.setAddress("Main Street 1");
					analyst.setBirthDate(LocalDate.of(1990, 2, 28));
					return internalAnalystUseCase.register(
							analyst,
							ANALYST_USERNAME,
							ANALYST_PASSWORD
					);
				});
	}

	private void seedCorporateCustomer() {
		if (userPort.existsByIdentificationId(CORPORATE_ID)) {
			return;
		}

		CorporateCustomer customer = new CorporateCustomer();
		customer.setBusinessName("Seed Corp S.A.");
		customer.setIdentificationId(CORPORATE_ID);
		customer.setEmail("corp@bank.test");
		customer.setPhone("3100000001");
		customer.setAddress("Business Avenue 100");
		customer.setLegalRepresentative("REP-10001");

		corporateCustomerUseCase.register(
				customer,
				CORPORATE_USERNAME,
				CORPORATE_PASSWORD
		);
	}

	private void seedCorporateEmployee() {
		if (userPort.existsByIdentificationId(CORPORATE_EMPLOYEE_ID)) {
			return;
		}

		User employee = new User();
		employee.setName("Seed Corporate Employee");
		employee.setIdentificationId(CORPORATE_EMPLOYEE_ID);
		employee.setEmail("corp.employee@bank.test");
		employee.setPhone("3100000002");
		employee.setAddress("Business Avenue 200");
		employee.setBirthDate(LocalDate.of(1992, 3, 20));
		employee.setRelatedId(CORPORATE_ID);

		corporateEmployeeUseCase.register(
				employee,
				CORPORATE_EMPLOYEE_USERNAME,
				CORPORATE_PASSWORD
		);
	}

	private void seedCorporateSupervisor() {
		if (userPort.existsByIdentificationId(CORPORATE_SUPERVISOR_ID)) {
			return;
		}

		User supervisor = new User();
		supervisor.setName("Seed Corporate Supervisor");
		supervisor.setIdentificationId(CORPORATE_SUPERVISOR_ID);
		supervisor.setEmail("corp.supervisor@bank.test");
		supervisor.setPhone("3100000003");
		supervisor.setAddress("Business Avenue 300");
		supervisor.setBirthDate(LocalDate.of(1988, 7, 12));
		supervisor.setRelatedId(CORPORATE_ID);

		corporateSupervisorUseCase.register(
				supervisor,
				CORPORATE_SUPERVISOR_USERNAME,
				CORPORATE_PASSWORD
		);
	}

	private User seedCommercialEmployee() {
		return userPort.findByIdentificationId(COMMERCIAL_EMPLOYEE_ID)
				.orElseGet(() -> {
					User employee = new User();
					employee.setName("Seed Commercial Employee");
					employee.setIdentificationId(COMMERCIAL_EMPLOYEE_ID);
					employee.setEmail("commercial@bank.test");
					employee.setPhone("3100000004");
					employee.setAddress("Commercial Avenue 10");
					employee.setBirthDate(LocalDate.of(1991, 6, 10));
					return commercialEmployeeUseCase.register(
							employee,
							COMMERCIAL_EMPLOYEE_USERNAME,
							CORPORATE_PASSWORD
					);
				});
	}

	private User seedTellerEmployee() {
		return userPort.findByIdentificationId(TELLER_EMPLOYEE_ID)
				.orElseGet(() -> {
					User employee = new User();
					employee.setName("Seed Teller Employee");
					employee.setIdentificationId(TELLER_EMPLOYEE_ID);
					employee.setEmail("teller@bank.test");
					employee.setPhone("3100000005");
					employee.setAddress("Teller Street 1");
					employee.setBirthDate(LocalDate.of(1993, 8, 10));
					return tellerEmployeeUseCase.register(
							employee,
							TELLER_EMPLOYEE_USERNAME,
							CORPORATE_PASSWORD
					);
				});
	}

	private void seedIndividualCustomer() {
		if (userPort.existsByIdentificationId(INDIVIDUAL_ID)) {
			return;
		}

		IndividualCustomer customer = new IndividualCustomer();
		customer.setName("Seed Individual");
		customer.setIdentificationId(INDIVIDUAL_ID);
		customer.setEmail("individual@bank.test");
		customer.setPhone("3200000001");
		customer.setBirthDate(LocalDate.of(1990, 5, 15));
		customer.setAddress("Residential Street 10");

		individualCustomerUseCase.register(
				customer,
				INDIVIDUAL_USERNAME,
				CORPORATE_PASSWORD
		);
	}

	private void seedAccounts(User teller, User commercial) {
		if (!accountPort.existsByAccountNumber(ACCOUNT_INDIVIDUAL)) {
			BankAccount account = new BankAccount();
			account.setProductCode("ACC-PERS-01");
			account.setProductName("Personal Savings");
			account.setCategory(Category.PERSONAL);
			account.setRequiresApproval(false);
			account.setAccountNumber(ACCOUNT_INDIVIDUAL);
			account.setAccountType(AccountType.SAVINGS);
			account.setAccountHolderId(INDIVIDUAL_ID);
			account.setBalance(500_000);
			account.setCurrency(Currency.COLOMBIAN_PESO);
			openAccount.openAccount(account, teller);
		}

		if (!accountPort.existsByAccountNumber(ACCOUNT_CORPORATE)) {
			BankAccount account = new BankAccount();
			account.setProductCode("ACC-BUS-01");
			account.setProductName("Business Checking");
			account.setCategory(Category.BUSINESS);
			account.setRequiresApproval(false);
			account.setAccountNumber(ACCOUNT_CORPORATE);
			account.setAccountType(AccountType.CHECKING);
			account.setAccountHolderId(CORPORATE_ID);
			account.setBalance(2_500_000);
			account.setCurrency(Currency.COLOMBIAN_PESO);
			openAccount.openAccount(account, commercial);
		}
	}
}
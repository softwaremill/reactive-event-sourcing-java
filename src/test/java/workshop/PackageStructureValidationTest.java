package workshop;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class PackageStructureValidationTest {

    private ImportOption ignoreTests = location -> !location.contains("/test-classes");
    private JavaClasses classes = new ClassFileImporter().withImportOption(ignoreTests).importPackages("workshop.cinema");

    private String baseModule = "..base..";
    private String reservationModule = "..reservation..";

    private String domainPackage = "..domain..";
    private String applicationPackage = "..application..";
    private String apiPackage = "..api..";
    private String infrastructurePackage = "..infrastructure..";
    private String akkaPackage = "..akka..";

    @Test
    public void shouldCheckDependenciesForDomainPackage() {
        //given
        ArchRule domainRules = noClasses()
                .that()
                .resideInAPackage(domainPackage)
                .should()
                .accessClassesThat()
                .resideInAPackage(applicationPackage)
                .orShould()
                .accessClassesThat()
                .resideInAPackage(apiPackage)
                .orShould()
                .accessClassesThat()
                .resideInAPackage(infrastructurePackage)
                .orShould()
                .accessClassesThat()
                .resideInAPackage(akkaPackage);

        // when // then
        domainRules.check(classes);
    }

    @Test
    public void shouldCheckDependenciesForApplicationPackage() {
        // given
        ArchRule applicationRules = noClasses()
                .that()
                .resideInAPackage(applicationPackage)
                .should()
                .accessClassesThat()
                .resideInAPackage(apiPackage)
                .orShould()
                .accessClassesThat()
                .resideInAPackage(infrastructurePackage);

        // when // then
        applicationRules.check(classes);
    }

    @Test
    public void shouldCheckDependenciesForApiPackage() {
        //given
        ArchRule apiRules = noClasses()
                .that()
                .resideInAPackage(apiPackage)
                .should()
                .accessClassesThat()
                .resideInAPackage(infrastructurePackage);

        // when // then
        apiRules.check(classes);
    }

    @Test
    public void shouldDependenciesForBasePackage() {
        // given
        ArchRule baseModuleRules = noClasses()
                .that()
                .resideInAPackage(baseModule)
                .should()
                .accessClassesThat()
                .resideInAPackage(reservationModule);

        // when // then
        baseModuleRules.check(classes);
    }
}

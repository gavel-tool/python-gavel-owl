import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;
import testAxiomTranslator.TestHasKey;

@RunWith(JUnitPlatform.class)
@SelectPackages({"testAxiomTranslator", "testClassExpressionTranslator"})
@SelectClasses({TestHasKey.class})
public class AxiomTestSuite {
}

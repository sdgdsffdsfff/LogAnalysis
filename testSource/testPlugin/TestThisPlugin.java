package testPlugin;

import com.hust.software.LogAnalysisInspection;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;

import java.util.List;
/**
 * Test the plugin
 * Created by Yan Yu on 2014-05-18.
 */
public class TestThisPlugin extends UsefulTestCase {

    protected CodeInsightTestFixture myFixture;
    // Specify path to your test data directory
    // e.g.  final String dataPath = "c:\\users\\john.doe\\idea\\community\\samples\\ComparingReferences/testData";
    final String dataPath = "F:\\work\\IdeaProjects\\LogAnalysis\\testData";


    public void setUp() throws Exception {

        final IdeaTestFixtureFactory fixtureFactory = IdeaTestFixtureFactory.getFixtureFactory();
        final TestFixtureBuilder<IdeaProjectTestFixture> testFixtureBuilder = fixtureFactory.createFixtureBuilder(getName());
        myFixture = JavaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(testFixtureBuilder.getFixture());
        myFixture.setTestDataPath(dataPath);
        final JavaModuleFixtureBuilder builder = testFixtureBuilder.addModule(JavaModuleFixtureBuilder.class);

        builder.addContentRoot(myFixture.getTempDirPath()).addSourceRoot("");
        builder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
        myFixture.setUp();
    }

    public void tearDown() throws Exception {
        myFixture.tearDown();
        myFixture = null;
    }

    protected void doTest(String testName, String hint) throws Throwable {
        myFixture.configureByFile(testName + ".java");
        myFixture.enableInspections(LogAnalysisInspection.class);
        List<HighlightInfo> highlightInfos = myFixture.doHighlighting();
        assertTrue(!highlightInfos.isEmpty());

        final IntentionAction action = myFixture.findSingleIntention(hint);

        assertNotNull(action);
        myFixture.launchAction(action);
        myFixture.checkResultByFile(testName + ".after.java");
    }
    // Test the condition analysis case
    public void test() throws Throwable {
//        System.out.print(LocalDate.now().atTime(0,0));
        doTest("before", "enhance log()");
    }
}

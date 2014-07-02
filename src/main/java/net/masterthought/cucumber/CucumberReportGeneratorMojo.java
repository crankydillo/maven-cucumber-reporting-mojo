package net.masterthought.cucumber;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.*;

/**
 * Goal which generates a Cucumber Report.
 *
 * @goal generate
 * @phase verify
 */
public class CucumberReportGeneratorMojo extends AbstractMojo {

    /**
     * Name of the project.
     *
     * @parameter expression="${project.name}"
     * @required
     */
    private String projectName;

    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}/cucumber-reports"
     * @required
     */
    private File outputDirectory;

    /**
     * Location of the file.
     *
     * @parameter expression="${cuc.output}" default-value=${project.build.directory}/cucumber.json"
     * @required
     */
    private File cucumberOutput;

    /**
     * Skipped fails
     *
     * @parameter expression="false"
     * @required
     */
    private Boolean skippedFails;

    /**
     * Undefined fails
     *
     * @parameter expression="false"
     * @required
     */
    private Boolean undefinedFails;

    /**
     * Enable Flash Charts.
     *
     * @parameter expression="true"
     * @required
     */
    private Boolean enableFlashCharts;

    public void execute() throws MojoExecutionException {
        List<String> list = new ArrayList<String>();
		for (File jsonFile : cucumberFiles(cucumberOutput)) {
			list.add(jsonFile.getAbsolutePath());
		}

		if (list.isEmpty()) {
			getLog().info("No .json files could be found in " +
					cucumberOutput.getAbsolutePath() + ".  No report will be generated.");
			return;
		}

		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}

        try {
            getLog().info("About to generate Cucumber report.");
            ReportBuilder reportBuilder = new ReportBuilder(list, outputDirectory, "", "1", projectName,
					skippedFails, undefinedFails, enableFlashCharts, false, false, "", false);
            reportBuilder.generateReports();

            boolean buildResult = reportBuilder.getBuildStatus();
            if (!buildResult) {
                throw new MojoExecutionException("BUILD FAILED - Check Report For Details");
            }

        } catch (Exception e) {
            throw new MojoExecutionException("Error Found:", e);
        }
    }

	// Normally, I'd keep this private and use mocks for testing the public contract.
	// I'm not sure that the author wants to get that serious with this..
	static Collection<File> cucumberFiles(File file) throws MojoExecutionException {
		// I'm not entirely convinced we should break the build in this case.
		if (!file.exists()) {
			return Collections.emptyList();
		}
		if (file.isFile()) {
			return Arrays.asList(file);
		}
		return FileUtils.listFiles(file, new String[] {"json"}, true);
	}
}

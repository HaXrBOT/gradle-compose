//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.tr7zw.gradle_compose;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.tr7zw.gradle_compose.provider.SourceProvider;
import dev.tr7zw.gradle_compose.util.ConfigUtil;
import dev.tr7zw.gradle_compose.util.FileProcessingUtil;
import dev.tr7zw.gradle_compose.util.FileUtil;
import dev.tr7zw.gradle_compose.util.GitUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.fusesource.jansi.AnsiConsole;

public class App {
	public final String version = "0.0.5";

	public App() {
	}

	public static void main(String[] args) {
		(new App()).run(args);
	}

	public void run(String[] args) {
		this.setup();
		this.printWelcome();
		this.applyExecFlag();
		ComposeData data = ConfigUtil.loadLocalConfig();
		SourceProvider provider = FileUtil.getProvider(data);
		TemplateData template = ConfigUtil.getTemplateData(provider, data);
		this.addAutoReplacements(data, template);
		this.processComposition(data, template, provider);
		provider.markAsDone();
	}

	private void setup() {
		AnsiConsole.systemInstall();
	}

	private void printWelcome() {
		System.out.println("Loading gradle-compose V0.0.5...");
	}

	private void applyExecFlag() {
		if (GitUtil.gitAvailable()) {
			System.out.println("Update exec flag of gradlecw...");
			GitUtil.runGitCommand(new File("."), new String[]{"git", "add", "--chmod=+x", "./gradlecw"});
		}

	}

	private void addAutoReplacements(ComposeData data, TemplateData template) {
		StringBuilder includes = new StringBuilder();
		Iterator var4 = data.subProjects.keySet().iterator();

		while(var4.hasNext()) {
			String entry = (String)var4.next();
			includes.append("include(\"" + entry + "\")\n");
		}

		data.replacements.put("autoincludes", includes.toString());
		StringBuilder githubWorkFlow = new StringBuilder();
		Iterator var14 = data.subProjects.keySet().iterator();

		while(var14.hasNext()) {
			String entry = (String)var14.next();
			githubWorkFlow.append("            " + entry + "/build/libs/*\n");
		}

		data.replacements.put("autoworkflowfiles", githubWorkFlow.toString());
		StringBuilder forkGithubWorkFlow = new StringBuilder();
		Iterator var16 = data.subProjects.keySet().iterator();

		while(var16.hasNext()) {
			String entry = (String)var16.next();
			forkGithubWorkFlow.append("            workspace/" + entry + "/build/libs/*\n");
		}

		data.replacements.put("githubautoworkflowfiles", forkGithubWorkFlow.toString());
		if (data.enabledFlags.contains("autopublish") && (new File("settings.json")).exists()) {
			Map<String, String> baseReplacements = FileProcessingUtil.mergeReplacements(new Map[]{data.replacements, template.defaultReplacements});
			StringBuilder releaseText = new StringBuilder();

			try {
				JsonObject settingsData = (JsonObject)(new Gson()).fromJson(new InputStreamReader(new FileInputStream(new File("settings.json"))), JsonObject.class);
				Iterator var9 = settingsData.get("versions").getAsJsonArray().iterator();

				while(true) {
					String version;
					do {
						if (!var9.hasNext()) {
							data.replacements.put("autoreleasesteps", releaseText.toString());
							return;
						}

						JsonElement el = (JsonElement)var9.next();
						version = el.getAsString();
						if (data.enabledFlags.contains("curseforge")) {
							releaseText.append("      - name: Publish-" + version + "-Curseforge\n");
							releaseText.append("        uses: Kir-Antipov/mc-publish@v" + (String)baseReplacements.get("mcpublishVersion") + "\n");
							releaseText.append("        with:\n");
							releaseText.append("          curseforge-id: " + (String)data.replacements.get("curseforgeid") + "\n");
							releaseText.append("          curseforge-token: ${{ secrets.CURSEFORGE_TOKEN }}\n");
							releaseText.append("          loaders: " + this.getModloaderName(version).toLowerCase() + "\n");
							releaseText.append("          name: " + data.replacements.get("name") + " v${{github.ref_name}} for "+ this.getModloaderName(version) + " " + this.getMCVersion(version) + "\n");
							if (this.isForgelike(version)) {
								releaseText.append("          version-type: beta\n");
							}

							if (!"1.20.6-forge".equals(version) && !"1.21-forge".equals(version)) {
								releaseText.append("          files: 'versions/" + version + "/build/libs/!(*-@(dev|sources|javadoc|all)).jar'\n");
							} else {
								releaseText.append("          files: 'versions/" + version + "/build/libs/!(*-@(dev|sources|javadoc)).jar'\n");
							}

							releaseText.append("          game-versions: " + this.getMCVersionDependency(version) + "\n");
							releaseText.append("          dependencies: " + data.replacements.get("mcpublish_dependencies") + "\n");
						}
					} while(!data.enabledFlags.contains("modrinth"));

					releaseText.append("      - name: Publish-" + version + "-Modrinth\n");
					releaseText.append("        uses: Kir-Antipov/mc-publish@v" + (String)baseReplacements.get("mcpublishVersion") + "\n");
					releaseText.append("        with:\n");
					releaseText.append("          modrinth-id: " + (String)data.replacements.get("modrinthid") + "\n");
					releaseText.append("          modrinth-token: ${{ secrets.MODRINTH_TOKEN }}\n");
					releaseText.append("          loaders: " + this.getModloaderName(version).toLowerCase() + "\n");
					releaseText.append("          name: " + data.replacements.get("name") + " v${{github.ref_name}} for "+ this.getModloaderName(version) + " " + this.getMCVersion(version) + "\n");
					if (!"1.20.6-forge".equals(version) && !"1.21-forge".equals(version)) {
						releaseText.append("          files: 'versions/" + version + "/build/libs/!(*-@(dev|sources|javadoc|all)).jar'\n");
					} else {
						releaseText.append("          files: 'versions/" + version + "/build/libs/!(*-@(dev|sources|javadoc)).jar'\n");
					}

					releaseText.append("          game-versions: " + this.getMCVersionDependency(version) + "\n");
					releaseText.append("          dependencies: " + data.replacements.get("mcpublish_dependencies") + "\n");
				}
			} catch (JsonIOException | FileNotFoundException | JsonSyntaxException var12) {
				var12.printStackTrace();
			}
		}

	}

	private String getMCVersionDependency(String version) {
		return "1.21".equals(version.split("-")[0]) ? "1.21.x" : version.split("-")[0];
	}

	private String getMCVersion(String version) {
		return "1.21".equals(version.split("-")[0]) ? "1.21(.1)" : version.split("-")[0];
	}

	private String getModloaderName(String version) {
		return version.toLowerCase().contains("fabric") ? "Fabric" : (version.toLowerCase().contains("neoforge") ? "NeoForge" : "Forge");
	}

	private boolean isForgelike(String version) {
		return version.toLowerCase().contains("forge");
	}

	private void processComposition(ComposeData data, TemplateData template, SourceProvider provider) {
		FileUtil.copyIfAvailable(provider, "gradle/wrapper/gradle-wrapper.properties");
		FileUtil.copyIfAvailable(provider, "gradle/wrapper/gradle-wrapper.jar");
		Map<String, String> baseReplacements = FileProcessingUtil.mergeReplacements(new Map[]{data.replacements, template.defaultReplacements});
		this.updateProject(new File("."), baseReplacements, provider, data.rootProject, template.availableFlags, data.enabledFlags);
		Iterator var5 = data.subProjects.entrySet().iterator();

		while(var5.hasNext()) {
			Map.Entry<String, ComposeData.Project> entry = (Map.Entry)var5.next();
			this.updateProject(new File(".", (String)entry.getKey()), baseReplacements, provider, (ComposeData.Project)entry.getValue(), template.availableFlags, data.enabledFlags);
		}

		if (data.version.equals("0.0.1")) {
			Set<String> customEntries = ConfigUtil.readCustomList(provider, "custom.compose");
			Iterator var9 = customEntries.iterator();

			while(var9.hasNext()) {
				String name = (String)var9.next();
				FileUtil.copyIfAvailableWithReplacments(provider, new File("."), data.rootProject.template, name, FileProcessingUtil.mergeReplacements(new Map[]{data.rootProject.replacements, baseReplacements}), template.availableFlags, data.enabledFlags);
			}
		} else {
			var5 = template.customEntries.iterator();

			while(var5.hasNext()) {
				String name = (String)var5.next();
				FileUtil.copyIfAvailableWithReplacments(provider, new File("."), data.rootProject.template, name, FileProcessingUtil.mergeReplacements(new Map[]{data.rootProject.replacements, baseReplacements}), template.availableFlags, data.enabledFlags);
			}
		}

		this.processFork(baseReplacements, provider, data.rootProject, template.availableFlags, data.enabledFlags);
	}

	private void updateProject(File baseDir, Map<String, String> replacements, SourceProvider provider, ComposeData.Project project, Set<String> availableTags, Set<String> enabledTags) {
		Map<String, String> projectReplacements = FileProcessingUtil.mergeReplacements(new Map[]{project.replacements, replacements});
		FileUtil.copyIfAvailableWithReplacments(provider, baseDir, project.template, "github/workflows/build.yml", ".github/workflows/build.yml", projectReplacements, availableTags, enabledTags);
		FileUtil.copyIfAvailableWithReplacments(provider, baseDir, project.template, "github/workflows/tag.yml", ".github/workflows/tag.yml", projectReplacements, availableTags, enabledTags);
		FileUtil.copyIfAvailableWithReplacments(provider, baseDir, project.template, "github/FUNDING.yml", ".github/FUNDING.yml", projectReplacements, availableTags, enabledTags);
		FileUtil.copyIfAvailableWithReplacments(provider, baseDir, project.template, "build.gradle", projectReplacements, availableTags, enabledTags);
		FileUtil.copyIfAvailableWithReplacments(provider, baseDir, project.template, "gradle.properties", projectReplacements, availableTags, enabledTags);
		FileUtil.copyIfAvailableWithReplacments(provider, baseDir, project.template, "settings.gradle", projectReplacements, availableTags, enabledTags);
	}

	private void processFork(Map<String, String> replacements, SourceProvider provider, ComposeData.Project project, Set<String> availableTags, Set<String> enabledTags) {
		File parentDir = new File("..");
		if ((new File(parentDir, "patches")).exists() && (new File(parentDir, "upstream")).exists() && (new File(parentDir, "workspace")).exists() && (new File(parentDir, "repo")).exists() && (new File(parentDir, "sha")).exists()) {
			System.out.println("Updating fork .github folder...");
			Map<String, String> projectReplacements = FileProcessingUtil.mergeReplacements(new Map[]{project.replacements, replacements});
			FileUtil.copyIfAvailableWithReplacments(provider, parentDir, project.template, "github/forkworkflows/build.yml", ".github/workflows/build.yml", projectReplacements, availableTags, enabledTags);
			FileUtil.copyIfAvailableWithReplacments(provider, parentDir, project.template, "github/forkworkflows/tag.yml", ".github/workflows/tag.yml", projectReplacements, availableTags, enabledTags);
			FileUtil.copyIfAvailableWithReplacments(provider, parentDir, project.template, "github/FUNDING.yml", ".github/FUNDING.yml", projectReplacements, availableTags, enabledTags);
		}

	}
}

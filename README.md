Smart Expense Splitter
======================

A small CLI application to split expenses, optimize debt settlement and produce simple analytics.

Getting started
---------------

Prerequisites
- Java 17 (JDK) installed. The project uses Java 17 source level.
- Maven 3.8+ installed if you want to use the Maven workflow or Jenkins pipeline.
- Jenkins on Ubuntu should have JDK 17 and Maven configured in Global Tool Configuration.

Run the app (preferred — single command)
```powershell
Set-Location E:\SmartExpenseSplitter
.\run.ps1            # interactive
.\run.ps1 -Demo      # read-only demo walkthrough
.\run.ps1 -DemoWrite # demo that exercises create/update flows (uses temporary dataset)
.\run.ps1 -DemoFull  # full seeded demo (adds member, creates expenses) and restores original sample data
# Or provide a custom command list:
.\run.ps1 -Commands @('help','list-users','exit')
```

Run with Maven
```powershell
mvn clean test
mvn clean package
mvn exec:java -Dexec.mainClass="Main"
java -jar target/expense-splitter.jar
```

How to make this repository a Maven project
1. Install Java 17 and Maven on your machine or Ubuntu build agent.
2. Keep the current Java sources under `src/`; the POM is configured to treat that folder as the Maven source root.
3. Use `pom.xml` as the build definition and run `mvn clean test` or `mvn clean package` from the repository root.
4. Use the provided `Jenkinsfile` for a pipeline build from Ubuntu.

Jenkins on Ubuntu
1. Install Jenkins, then install the recommended plugins for Pipeline, Git, and JUnit.
2. Configure tool names in Jenkins Global Tool Configuration to match the pipeline: `Maven3` and `JDK17`.
3. Create a new Pipeline job and select Pipeline script from SCM or point it at this repository.
4. Let Jenkins read the root `Jenkinsfile` and run `Checkout`, `Build`, `Test`, and `Package`.
5. Archive the JAR from `target/` and publish test results from `target/surefire-reports/`.

About `.vscode` and `.github`
- `.vscode/` stores editor settings, launch configurations and workspace recommendations. Keep it if you want reproduceable editor behaviour across machines; otherwise you can remove it locally or add it to `.gitignore`.
- `.github/` stores GitHub metadata and Actions workflows. Keep it if you rely on CI, PR/issue templates or CODEOWNERS. Removing it will disable GitHub Actions in this repo.

If you want to stop tracking them in git but keep local copies, run from the repo root:
```powershell
mkdir backup-configs
Copy-Item -Recurse -Force .vscode backup-configs\.vscode
Copy-Item -Recurse -Force .github backup-configs\.github
git rm -r --cached .vscode
git rm -r --cached .github
Add-Content .gitignore ".vscode/"
Add-Content .gitignore ".github/"
git add .gitignore
git commit -m "Stop tracking editor and GitHub configs (.vscode, .github)"
git push origin main
```

Branch note — pushing to `main`
If your remote default branch was `master` and you want to use `main`: rename locally (`git branch -m master main`) or create `main` from current HEAD and push with `git push -u origin main`. Update the repository default branch on GitHub (Settings → Branches) or use the `gh` CLI.

Where to look next
- The launcher is `run.ps1` in the project root — it compiles the flat `src/` layout and runs `Main`.
- Sample data lives in `data/` and demo modes restore the original sample files after execution.

If you want I can update the repository to stop tracking `.vscode` and `.github` now (I will prepare and run the git commands only after you confirm).

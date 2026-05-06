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

Jenkins Demo Modes Input Reference
----------------------------------
The pipeline parameter `DEMO_MODE` controls which CLI commands are fed to the app.

How input is passed in Jenkins
1. The selected command block is written to `target/commands.txt`.
2. Jenkins runs: `java -jar target/expense-splitter.jar < target/commands.txt`.
3. Console output is also saved to `target/app-output.log`.

Data Used By These Demo Commands (Current Seed Data)
----------------------------------------------------
All mode examples above assume the default files in `data/`:

`data/users.json`
- `a1b2` -> Alice
- `c3d4` -> Bob

`data/groups.json`
- `g1` -> Trip
- members: `a1b2`, `c3d4`

`data/expenses.json`
- one expense `e1`: Dinner, total `300.00`
- paid by `a1b2` (Alice)
- group: `g1`
- split type: `EQUAL`
- splits:
	- Alice share: `150.00`
	- Bob share: `150.00`

What this means financially for the demo
- Alice paid `300.00` but her own share is `150.00`, so she should receive `150.00` back.
- Bob paid `0.00` and his share is `150.00`, so he owes `150.00`.
- Net expected settlement in `g1`: Bob -> Alice `150.00`.

`BASIC` mode commands
```text
help
list-users
list-groups
list-expenses g1
show-balances g1
exit
```

What this shows
- basic navigation/help
- viewing users and groups
- listing expenses in group `g1`
- viewing balances for `g1`

`ANALYTICS` mode commands
```text
help
list-expenses g1
show-balances g1
show-balance-between a1b2 c3d4
settle g1
analytics-paid
analytics-owed
largest-debtor
largest-creditor
exit
```

What this shows
- group expense and balance views
- pairwise balance check (`show-balance-between`)
- settlement recommendation (`settle g1`)
- analytics reports and top debtor/creditor

`FULL` mode commands
```text
help
list-users
list-groups
list-expenses g1
show-balances g1
show-balance-between a1b2 c3d4
settle g1
analytics-paid
analytics-owed
largest-debtor
largest-creditor
exit
```

What this shows
- combines `BASIC` and `ANALYTICS` flows in one run

`CUSTOM` mode commands
- Jenkins reads commands from parameter `APP_COMMANDS` (one command per line).
- Use this to demo a specific path without changing the pipeline code.

Detailed Walkthrough (What Each Command Does)
--------------------------------------------
Use this section when presenting the demo so the audience understands what is happening at each input step.

Common commands used in the modes

`help`
- Prints the available CLI commands and accepted argument format.
- Purpose: confirms the app is running and shows the command surface.

`list-users`
- Displays all users loaded from the current dataset.
- Purpose: verify user master data before financial operations.

`list-groups`
- Displays all groups currently available.
- Purpose: confirm which group IDs are valid for follow-up commands.

`list-expenses g1`
- Lists expense records in group `g1`.
- Typical info shown: expense description, payer, amount, split details.
- With current data: should show Dinner (`300.00`) paid by Alice, split equally between Alice and Bob.
- Purpose: show raw transaction-level data before computing balances.

`show-balances g1`
- Computes and prints who owes whom inside group `g1`.
- With current data: expected net is Bob owes Alice `150.00`.
- Purpose: show net balances after applying all expense splits.

`show-balance-between a1b2 c3d4`
- Shows pairwise net balance between users `a1b2` and `c3d4`.
- With current data: expected pairwise result is Bob (`c3d4`) owes Alice (`a1b2`) `150.00`.
- Purpose: demonstrate bilateral debt view in addition to group-level view.

`settle g1`
- Runs debt simplification for group `g1`.
- Output is a minimized settlement plan (fewer transactions while preserving net outcomes).
- With current data: expected settlement recommendation is a single transaction of `150.00` from Bob to Alice.
- Purpose: demonstrate optimization logic (who should pay whom to settle efficiently).

`analytics-paid`
- Shows users ranked or summarized by total amount paid.
- With current data: Alice should appear highest (paid `300.00`), Bob paid `0.00`.
- Purpose: identify top contributors/spenders.

`analytics-owed`
- Shows users ranked or summarized by total amount owed.
- With current data: Bob should appear as owing the most in net terms (`150.00`).
- Purpose: identify users with highest liabilities.

`largest-debtor`
- Prints the user with the highest net debt.
- With current data: expected largest debtor is Bob.
- Purpose: quick single-metric insight for reporting/demo.

`largest-creditor`
- Prints the user with the highest net credit.
- With current data: expected largest creditor is Alice.
- Purpose: quick single-metric insight for reporting/demo.

`exit`
- Gracefully closes the CLI session.
- Purpose: deterministic end of scripted demo input.

Mode-by-Mode Explanation
------------------------

`BASIC` (core flow)
- Focus: data visibility and basic balance computation.
- Sequence logic:
	1. Start with `help` so command list is visible.
	2. Confirm entities using `list-users` and `list-groups`.
	3. Inspect transactions with `list-expenses g1`.
	4. Compute financial result with `show-balances g1`.
	5. End with `exit`.
- Best for: first-time walkthrough, non-technical audience.

`ANALYTICS` (insight flow)
- Focus: settlement and analytical insights.
- Sequence logic:
	1. Load context (`help`, `list-expenses g1`, `show-balances g1`).
	2. Drill down with `show-balance-between a1b2 c3d4`.
	3. Run simplification with `settle g1`.
	4. Present insights via `analytics-paid`, `analytics-owed`, `largest-debtor`, `largest-creditor`.
	5. End with `exit`.
- Best for: showing business value and decision support outputs.

`FULL` (end-to-end flow)
- Focus: combines entity listing + balances + settlement + analytics.
- Sequence logic: runs both coverage areas in one script.
- Best for: complete product demonstration in a single Jenkins run.

`CUSTOM` (scenario-driven flow)
- Focus: your own scripted scenario through `APP_COMMANDS`.
- Rules:
	- one command per line
	- use valid IDs present in your dataset (in current seed: users `a1b2`, `c3d4`; group `g1`)
	- end with `exit` for clean termination

Example `APP_COMMANDS`
```text
help
list-users
list-expenses g1
show-balances g1
settle g1
exit
```

How to Read the Demo Output
---------------------------
- Input file used by Jenkins: `target/commands.txt`
- Full captured app output: `target/app-output.log`
- In Jenkins console, each command executes in order exactly as written in the selected mode.
- If a command fails (for example invalid user/group ID), later commands may still run, but results can be incomplete for that scenario.

Presentation Tips
-----------------
- Use `BASIC` for quick functionality proof.
- Use `ANALYTICS` when discussing optimization/reporting features.
- Use `FULL` for final end-to-end demonstration.
- Use `CUSTOM` for stakeholder-specific questions during live demo.

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

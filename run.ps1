param(
    [string[]]$Commands,
    [switch]$Demo,
    [switch]$DemoWrite,
    [switch]$DemoFull
)

$ErrorActionPreference = 'Stop'

Set-Location $PSScriptRoot

$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
    $javaHome = 'C:\Users\Asus\.jdk\jdk-17.0.18'
}

if (-not (Test-Path $javaHome)) {
    throw "Java home not found: $javaHome"
}

$env:JAVA_HOME = $javaHome
$env:Path = "$javaHome\bin;$($env:Path)"

$sourceFiles = Get-ChildItem -Path (Join-Path $PSScriptRoot 'src') -Recurse -Filter *.java | Where-Object { $_.FullName -notlike '*\test\*' } | ForEach-Object { $_.FullName }
if (-not $sourceFiles) {
    throw 'No Java source files were found under src/.'
}

$classesDir = Join-Path $PSScriptRoot 'target\classes'
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null

& "$javaHome\bin\javac.exe" -encoding UTF-8 --release 17 -d $classesDir @sourceFiles

function Invoke-ExpenseApp {
    param([string[]]$InputCommands)

    if ($InputCommands -and $InputCommands.Count -gt 0) {
        ($InputCommands -join [Environment]::NewLine) | & "$javaHome\bin\java.exe" -cp $classesDir Main
    } else {
        & "$javaHome\bin\java.exe" -cp $classesDir Main
    }
}

function Use-DemoDataset {
    param(
        [scriptblock]$Action
    )

    $dataDir = Join-Path $PSScriptRoot 'data'
    $backupDir = Join-Path $PSScriptRoot 'target\demo-backup'
    New-Item -ItemType Directory -Force -Path $backupDir | Out-Null

    $files = @('users.json', 'groups.json', 'expenses.json')
    foreach ($file in $files) {
        $source = Join-Path $dataDir $file
        $backup = Join-Path $backupDir $file
        if (Test-Path $source) {
            Copy-Item $source $backup -Force
        }
    }

    try {
        Set-Content -Path (Join-Path $dataDir 'users.json') -Value @'
[
  {"id":"a1b2","name":"Alice","email":"alice@mail.com"},
  {"id":"c3d4","name":"Bob","email":"bob@mail.com"},
  {"id":"e5f6","name":"Charlie","email":"charlie@mail.com"}
]
'@ -Encoding UTF8

        Set-Content -Path (Join-Path $dataDir 'groups.json') -Value @'
[
  {"id":"g1","name":"Trip","memberIds":["a1b2","c3d4"]}
]
'@ -Encoding UTF8

        Set-Content -Path (Join-Path $dataDir 'expenses.json') -Value @'
[
  {"id":"e1","description":"Dinner","totalAmount":300.00,"paidById":"a1b2","groupId":"g1","splitType":"EQUAL","date":"2025-04-01","splits":[{"userId":"a1b2","amount":150.00,"percentage":0.00},{"userId":"c3d4","amount":150.00,"percentage":0.00}]}
]
'@ -Encoding UTF8

        & $Action
    }
    finally {
        foreach ($file in $files) {
            $backup = Join-Path $backupDir $file
            $target = Join-Path $dataDir $file
            if (Test-Path $backup) {
                Copy-Item $backup $target -Force
            }
        }
    }
}

if ($DemoWrite) {
    $demoTag = Get-Date -Format 'yyyyMMddHHmmss'
    $demoYear = (Get-Date).Year
    $demoMonth = (Get-Date).Month
    Use-DemoDataset {
        Invoke-ExpenseApp -InputCommands @(
            'help'
            "add-user DemoUser${demoTag} demo${demoTag}@mail.com"
            "add-group DemoGroup${demoTag} a1b2 c3d4"
            "add-expense g1 Lunch${demoTag} 120 a1b2 EQUAL a1b2 c3d4"
            "add-expense g1 Ride${demoTag} 90 c3d4 PERCENTAGE a1b2 40 c3d4 60"
            "add-expense g1 Exact${demoTag} 60 a1b2 EXACT a1b2 20 c3d4 40"
            'list-users'
            'list-groups'
            'list-expenses g1'
            'show-balances g1'
            'show-balance-between a1b2 c3d4'
            'settle g1'
            'analytics-paid'
            'analytics-owed'
            'analytics-share g1'
            "monthly-report g1 $demoYear $demoMonth"
            'largest-debtor'
            'largest-creditor'
            'exit'
        )
    }
    return
}

if ($Demo) {
    Use-DemoDataset {
        Invoke-ExpenseApp -InputCommands @(
            'help'
            'list-users'
            'list-groups'
            'list-expenses g1'
            'show-balances g1'
            'show-balance-between a1b2 c3d4'
            'settle g1'
            'analytics-paid'
            'analytics-owed'
            'analytics-share g1'
            'monthly-report g1 2025 4'
            'largest-debtor'
            'largest-creditor'
            'exit'
        )
    }
    return
}

if ($DemoFull) {
    Use-DemoDataset {
        Invoke-ExpenseApp -InputCommands @(
            'help'
            'list-users'
            'list-groups'
            'add-member g1 e5f6'
            'add-expense g1 Brunch 120 a1b2 EQUAL a1b2 c3d4 e5f6'
            'add-expense g1 Cab 90 c3d4 PERCENTAGE a1b2 30 c3d4 30 e5f6 40'
            'add-expense g1 Snacks 60 a1b2 EXACT a1b2 10 c3d4 20 e5f6 30'
            'list-expenses g1'
            'show-balances g1'
            'show-balance-between a1b2 c3d4'
            'settle g1'
            'analytics-paid'
            'analytics-owed'
            'analytics-share g1'
            'monthly-report g1 2025 4'
            'largest-debtor'
            'largest-creditor'
            'exit'
        )
    }
    return
}

if ($Commands -and $Commands.Count -gt 0) {
    Invoke-ExpenseApp -InputCommands $Commands
} else {
    Invoke-ExpenseApp
}

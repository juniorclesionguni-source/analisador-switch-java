# Compila o projecto (Windows / PowerShell).
# Uso:  .\compilar.ps1
$base = $PSScriptRoot
$srcs = Get-ChildItem -Recurse -Path "$base\src" -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d "$base\out" $srcs
if ($?) {
    Write-Host "Compilacao OK. Para executar:"
    Write-Host "  java -cp `"$base\out`" Main testes\t1_valido_simples.txt"
} else {
    Write-Host "Compilacao FALHOU."
}

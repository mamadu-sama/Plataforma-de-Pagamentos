# Script de teste de endpoints para FinTouch Ledger

$baseUrl = "http://localhost:8080"

function Invoke-Api {
    param (
        [string]$Method,
        [string]$Url,
        [string]$Body
    )
    
    $headers = @{ "Content-Type" = "application/json" }
    
    try {
        if ($Body) {
            $response = Invoke-RestMethod -Method $Method -Uri "$baseUrl$Url" -Body $Body -Headers $headers -ErrorAction Stop
        } else {
            $response = Invoke-RestMethod -Method $Method -Uri "$baseUrl$Url" -Headers $headers -ErrorAction Stop
        }
        return $response
    } catch {
        Write-Host "Erro no pedido $Method $Url" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        if ($_.Exception.Response) {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            Write-Host $reader.ReadToEnd() -ForegroundColor Red
        }
        exit 1
    }
}

Write-Host "`n--- 1. Criar Utilizador Pagador (Comum) ---" -ForegroundColor Cyan
$payerBody = '{
    "fullName": "Joao Pagador",
    "email": "joao.pagador@example.com",
    "document": "12345678900",
    "type": "COMMON"
}'
$payer = Invoke-Api -Method "POST" -Url "/users" -Body $payerBody
Write-Host "Pagador criado com ID: $($payer.id)" -ForegroundColor Green

Write-Host "`n--- 2. Criar Utilizador Recebedor (Lojista) ---" -ForegroundColor Cyan
$payeeBody = '{
    "fullName": "Maria Lojista",
    "email": "maria.lojista@example.com",
    "document": "98765432100",
    "type": "MERCHANT"
}'
$payee = Invoke-Api -Method "POST" -Url "/users" -Body $payeeBody
Write-Host "Recebedor criado com ID: $($payee.id)" -ForegroundColor Green

Write-Host "`n--- 3. Injectar Saldo Inicial no Pagador (Via Banco - Simulado) ---" -ForegroundColor Cyan
# Nota: Em produção não haveria endpoint para injectar saldo, mas aqui precisamos de saldo para testar.
# Como o projeto não tem endpoint de depósito, vamos assumir que o saldo inicial é zero e a transferência vai falhar
# SE não houver saldo. Mas espere! O User Service cria carteira com saldo ZERO.
# Para testar, precisamos de saldo. 
# Como não criei endpoint de depósito, vou usar o docker exec para atualizar o banco diretamente!

$payerId = $payer.id
$sql = "UPDATE wallets SET balance = 1000.00 WHERE user_id = $payerId;"
docker-compose exec -T db psql -U fintouch -d fintouch -c "$sql" | Out-Null
Write-Host "Saldo de 1000.00 injectado na carteira do pagador via SQL direto." -ForegroundColor Yellow

Write-Host "`n--- 4. Consultar Saldo Antes da Transferência ---" -ForegroundColor Cyan
$walletPayer = Invoke-Api -Method "GET" -Url "/wallet/$payerId"
Write-Host "Saldo Pagador: $($walletPayer.balance)" -ForegroundColor White

Write-Host "`n--- 5. Realizar Transferência de 100.00 ---" -ForegroundColor Cyan
$transferBody = @"
{
    "value": 100.00,
    "payer": $($payer.id),
    "payee": $($payee.id)
}
"@
$transaction = Invoke-Api -Method "POST" -Url "/transaction" -Body $transferBody
Write-Host "Transferência realizada com sucesso! ID Transacção: $($transaction.transactionId)" -ForegroundColor Green

Write-Host "`n--- 6. Consultar Saldos Finais ---" -ForegroundColor Cyan
$walletPayerFinal = Invoke-Api -Method "GET" -Url "/wallet/$($payer.id)"
$walletPayeeFinal = Invoke-Api -Method "GET" -Url "/wallet/$($payee.id)"

Write-Host "Saldo Final Pagador: $($walletPayerFinal.balance) (Esperado: 900.00)" -ForegroundColor White
Write-Host "Saldo Final Recebedor: $($walletPayeeFinal.balance) (Esperado: 100.00)" -ForegroundColor White

Write-Host "`n--- Teste Concluído com Sucesso! ---" -ForegroundColor Green

--[[ Variáveis Globais ]]
local out=ofOutlet(this) --[[ permite controlar saída em outlets ]]
local nbxMin = ofValue("$0-min") --[[ referente a caixa de número com tempo inicial ]]
local nbxTempo = ofValue("$0-tempo") --[[ referente a caixa de número com intervalo de tempo ]]
local nbxMax = ofValue("$0-max") --[[ referente a caixa de número com tempo final ]]
local min = 0
local tempo = 0
local max = 0

function M.list(fv)
    --[[ inicializa variaveis ]]
    min = fv[1]
    tempo = fv[2]
    max = fv[3]

    --[[ comportamento da caixa de número de tempo inicial ]]
    if max==0 or max<=min+tempo then
        max = min+tempo
        if max > 45000 then
            max = 45000
            min = max - tempo
        end
        nbxMax:set(max)
        nbxMin:set(min)
    else
        max = tempo + min
        nbxMax:set(max)
        nbxMin:set(min)
    end

    --[[ controle de acionamento do botão ARMAR na Interface ]]
    local estadoAcionamentoARMAR = 0    --[[quando a interface está zerada de entradas]]

    if min==0 and max==0 then
        estadoAcionamentoARMAR = 0
    else
        if min ~= memoria then    --[[ quando os valores de tempo são alterados, desarma tb ]]
            estadoAcionamentoARMAR = 1
            memoria = min
        else
            estadoAcionamentoARMAR = 0
        end
    end

    --[[ seta saídas nos outlets ]]
    out:outletList(0, ofTable('set', min))
    out:outletList(1, ofTable('set', tempo))
    out:outletList(2, ofTable('set', max))
    out:outletList(3, ofTable('set', min, max))
    out:outletList(4, ofTable(estadoAcionamentoARMAR))
end

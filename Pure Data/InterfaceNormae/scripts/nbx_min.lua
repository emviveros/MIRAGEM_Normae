--[[ Variáveis Globais ]]
local out=ofOutlet(this) --[[ permite controlar saída em outlets ]]
local nbxMin = ofValue("$0-min") --[[ referente a caixa de número com tempo inicial ]]
local nbxTempo = ofValue("$0-tempo") --[[ referente a caixa de número com intervalo de tempo ]]
local nbxMax = ofValue("$0-max") --[[ referente a caixa de número com tempo final ]]

function M.list(fv)
    --[[ inicializa variaveis ]]
    local min = fv[1]
    local tempo = fv[2]
    local max = fv[3]

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

    --[[ seta saídas nos outlets ]]
    local out0 = ofTable('set', min)
    local out1 = ofTable('set', tempo)
    local out2 = ofTable('set', max)
    local out3 = ofTable('set', min, max)
    out:outletList(0, out0)
    out:outletList(1, out1)
    out:outletList(2, out2)
    out:outletList(3, out3)
end

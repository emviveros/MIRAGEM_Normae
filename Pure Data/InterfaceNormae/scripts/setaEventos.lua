--[[ Variáveis Globais ]]
local out = ofOutlet(this) --[[ permite controlar saída em outlets ]]
local arrayTemposIniciais = ofArray("$0-temposIniciais")
local arrayTemposFinais = ofArray("$0-temposFinais")
local temposIniciais = {}
local temposFinais = {}
local Porta = 0
local tempoInicial = 0
local tempoFinal = 0
local evento = 1
local saida = ofTable(evento, Porta, tempoInicial, tempoFinal)

local function atualiza_vetores_de_tempos_iniciais_e_finais()
    temposIniciais = arrayTemposIniciais:get(0)
    temposFinais = arrayTemposFinais:get(0)

    -- for index, value in ipairs(temposIniciais) do
    --     print('temposIniciais'..'['..index..'] = ', value)
    -- end
    -- for index, value in ipairs(temposFinais) do
    --     print('temposFinais'..'['..index..'] = ', value)
    -- end
end


local function cicloPortasTerminou()
    local retorno = true
    if Porta <= 8 then
        retorno = false
    else
        out:outletFloat(2, 0)   --[[ interrompe timer ]]
        Porta = 0
        retorno = true
        --[[ Mensagem de finalização ]]
        print('Terminou de setar Eventos')
    end
    return retorno
end


local function enviaEvento()
    tempoInicial = temposIniciais[Porta]
    -- print('debug enviaEvento() tempoInicial =', tempoInicial)
    tempoFinal = temposFinais[Porta]
    -- print('debug enviaEvento() tempoFinal =', tempoFinal)
    saida = {evento, Porta, tempoInicial, tempoFinal}
    if tempoFinal ~= 0 then
        out:outletList(1, saida)
        out:outletAnything(0,{"bang"})
    end
end


function M.setaEvento()
    atualiza_vetores_de_tempos_iniciais_e_finais()

    --[[ seta saídas nos outlets ]]
    out:outletFloat(2, 1) --[[ libera timer ]]
end

function M.timer()
    Porta = Porta + 1
    -- print('funcão M.timer - Porta ='..Porta)
    if not cicloPortasTerminou() then
        enviaEvento()
    end
end
    

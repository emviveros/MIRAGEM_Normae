//=======================================================================================

/* ToDo
 *  
 *  - Botão de Emergência (interrompe operação)
 *      (talvez seja necessário trabalhar com interrupções)
 *    
 *    - no Processing (interface)
 *        - adicionar botão de Reset (zera tempos de EVENTOs e seta TEMPO MÁXIMO padrão)
 *            - exibir botão de Reset no lugar do botão INICIA enquanto
 *              Normae estiver em operação (/operacao/1 )
 *        - adicionar opção de resetar interface (todos os eventos zerados)
 *        - adicionar suporte a Presets
 *
 *  - Implementar uma segurança real para a confirmação de que a Normae está armada,
 *    pronta para operar na função: mensagemDeArmado
 *
 */

//=======================================================================================

/*
 * Observações:
 *  
 *  - Pude observar flutuações de até 7 ms. Talvez sejam fruto do loop incluido
 *    a leitura de mensagens OSC. Uma possibilidade para implementar a função
 *    de interromper a operação durante uma operação seria de um botão externo
 *    que resete o Arduino ou que chame uma função de reset.
 *      - Um botão que chame uma função de reset só poderia ser implementada
 *        substituindo bibliotecas de comunicação a ser verificada (vide obs abaixo)
 *  
 *  - Suporte a configuração de uso da variável portaSync para IniciarEventos
 *    da Normae via Interface em Processing, comunicando-se via OSC
 *    (Impossível de implementar usando estas bibliotecas: leituras instáveis
 *     nas portas de entrada. Talvez pelo uso das bibliotecas de comunicação)
 *      - Porta para acionamento de eventos programados via sensor externo setado 
 *         na porta A15. (tensão de 3.3V) (procurar no código por: Sensor Externo)
 *      - variável portaSync indica se a porta de acionamento via sensor externo
 *        está ativada ou não.
 *  
 *  - Em caso de precisar receber um sinal de 5V para iniciar operação verificar link:
 *    https://www.arduino.cc/reference/en/language/functions/advanced-io/pulsein/
 *    pode resolver anexando pulsein() na porta dentro função void iniciaOperacao()
 *    lembrar de setar pullup porta: pinMode(CHAVE, INPUT_PULLUP);
 *    http://eletronicaparaartistas.com.br/arduino-5-resistores-de-pull-up/
 *  
 */

//=======================================================================================

/*
   Controle de portas digitais do Arduino MEGA 2560
   através de mensagens recebidas pela porta serial.

   Uso do Módulo controlador 8 de relés eletromecânicos
   do Shiyozi.


    -------------------------------
   | MODULO RELÉS  | Pino Arduino  |
   |---------------|---------------|
   |    IN1        |      22       |
   |    IN2        |      24       |
   |    IN3        |      26       |
   |    IN4        |      28       |
   |    IN5        |      30       |
   |    IN6        |      32       |
   |    IN7        |      34       |
   |    IN8        |      36       |
    -------------------------------

*/

//=======================================================================================
// Bibliotecas para módulo Ethernet Shield W5100
//=======================================================================================

#include <SPI.h>
#include <Ethernet.h>
#include <EthernetUdp.h>


//=======================================================================================
// Biblioteca OSC
//=======================================================================================

#include <OSCBundle.h>


//=======================================================================================
// Instruções referentes à controladora de reles
//=======================================================================================

#include "RelayModule.hpp"


//=======================================================================================
// Configuração da interface Ethernet
//=======================================================================================

// endereço IP da interface de ethernet
static byte ip[] = {  169, 254, 68, 51 };  //para usar com PC
//static byte ip[] = { 169, 254, 81, 77 };    // para testar no RPi

// endereço IP do computador com a tela touch
static byte rpi_ip[] = { 169, 254, 68, 52 };

// endereço mac para interface ethernet (precisa ser único na rede)
static byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED };

// porta Udp de recebimento
const unsigned int inPort = 5555;

// porta Udp de envio
const unsigned int outPort = 7777;


//=======================================================================================
// Variáveis Globais
//=======================================================================================

unsigned long tempoTotal;   // tempo total de operação (recebido por mensagem OSC)
unsigned long millis_inicial;
boolean podeIr;


//=======================================================================================
// Declaração de funções
//=======================================================================================

void OSCMsgReceive();
void imprimeIP();
void setaModoAtivo(OSCMessage &msg, int addrOffset);
void setaTempoTotal(OSCMessage &msg, int addrOffset);


//=======================================================================================
// Inicialização de objetos
//=======================================================================================

// Instancia objeto que cria mensagens UDP
EthernetUDP Udp;

// Instancia controlador de reles
RelayModule rele_tensao(22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, false);

RelayModule portas(23, 25, 27, 29, 31, 33, 35, 37, 39, 41, 43, 45, true);


//=======================================================================================
// Classe Porta - Classe que controla eventos em porta da Normae
//=======================================================================================

class Porta
{
  private:
    int numeroDaPorta;
  
  public:
    Porta(int valor)
    {
      setaPorta(valor);
    }

    ///////////////////////////////////////
    // função que seta a variável numeroDaPorta
    ///////////////////////////////////////
    void setaPorta(int numero)
    {
      numeroDaPorta = numero;
    }


    ///////////////////////////////////////
    // sinaliza se tem alguma atividade a ser realizada na Porta
    ///////////////////////////////////////
    boolean atividadeNaPorta = false;

    ///////////////////////////////////////
    // true se existe agendamento em algum (evento - 1) nesta porta
    ///////////////////////////////////////
    boolean eventoInicializado[7] = { false, false, false, false, false, false, false };


    ///////////////////////////////////////
    // função que retorna true se o evento está inicializado
    ///////////////////////////////////////
    boolean foiInicializado(int evento)
    {
      return eventoInicializado[evento -1];
    }


    ///////////////////////////////////////
    // se deve rodar evento, serve pra dizer o de há atividade sendo realizada
    //em determinado evento da porta
    ///////////////////////////////////////
    boolean eventoAtivo[7] = { false, false, false, false, false, false, false };


    ///////////////////////////////////////
    // função que retorna true se o evento está ativo
    ///////////////////////////////////////
    boolean estaAtivado(int evento)
    {
      return eventoAtivo[evento -1];
    }


    ///////////////////////////////////////
    // tempo Inicial de cada um dos eventos na Porta
    ///////////////////////////////////////
    unsigned long tempoInicial[7] = { 0, 0, 0, 0, 0, 0, 0 };

    ///////////////////////////////////////
    // tempo Final de cada um dos eventos na Porta
    ///////////////////////////////////////
    unsigned long tempoFinal[7] = { 0, 0, 0, 0, 0, 0, 0 };


    ///////////////////////////////////////
    // retorna tempo Inicial de cada um dos eventos na Porta
    ///////////////////////////////////////
    unsigned long retorna_tempoInicial(int evento)
    {
      return tempoInicial[evento - 1];
    }

    ///////////////////////////////////////
    // retorna tempo Final de cada um dos eventos na Porta
    ///////////////////////////////////////
    unsigned long retorna_tempoFinal(int evento)
    {
      return tempoFinal[evento - 1];
    }


    ///////////////////////////////////////
    // função para configurar tempo inicial de determinado evento
    ///////////////////////////////////////
    void setarTempoInicial (int evento, unsigned long tempo)
    {
      tempoInicial[evento - 1] = tempo;
    }


    ///////////////////////////////////////
    // função para configurar tempo final de determinado evento
    ///////////////////////////////////////
    void setarTempoFinal (int evento, unsigned long tempo)
    {
      tempoFinal[evento - 1] = tempo;
      
      if (tempo != 0)   // Inicializa evento na porta
      {
        eventoInicializado[evento - 1] = true;
      }
      else
      {
        eventoInicializado[evento - 1] = false;
      }

      setaAtividadeNaPorta();
    }

    ///////////////////////////////////////
    // função para configurar se existe evento a ser realizado na Porta
    ///////////////////////////////////////
    void setaAtividadeNaPorta()
    {
      if ( maiorTempoFinal() == 0)
      {
        atividadeNaPorta = false;
      }
      else
      {
        atividadeNaPorta = true;
      }
    }

    ///////////////////////////////////////
    // função que retorna maior elemento do array tempoFinal
    ///////////////////////////////////////
    unsigned long maiorTempoFinal()
    {
      unsigned long maiorValor = 0;
      for (int i = 0; i < 7; i++)
      {
        maiorValor = max( tempoFinal[i], maiorValor );
      }
      return maiorValor;
    }


    ///////////////////////////////////////
    // função que retorna se ainda existe alguma atividade
    //  a ser realizada pela Porta
    ///////////////////////////////////////
    boolean temAtividade()
    {
      unsigned long maiorValor = 0;
      for (int i = 0; i < 7; i++)
      {
        maiorValor = max( tempoFinal[i], maiorValor );
      }
      
      if ( (millis() - millis_inicial) <= maiorValor )
      {
        return true;
      }
      else
      {
        return false;
      }
    }


    ///////////////////////////////////////
    // função para configurar estado de evento da porta.
    //  O array eventoAtivo[ ] indica se a porta está ativada
    //  (true = ativada)
    ///////////////////////////////////////
    void setarEventoAtivo (int evento, boolean estado)
    {
      eventoAtivo[evento - 1] = estado;
    }



    ///////////////////////////////////////
    // função reset
    ///////////////////////////////////////
    void (*funcReset) () = 0;

    
    ///////////////////////////////////////
    // função que processa as mensagens OSC de reset recebidas
    ///////////////////////////////////////
    void OSCMsgReceiveReset()
    {
      OSCMessage msgIN;
      int size;
      if((size = Udp.parsePacket())>0)
      {
        while(size--)
          msgIN.fill(Udp.read());
        if(!msgIN.hasError())
        {
          msgIN.route("/reset", funcReset);
        }
      }
    }


    ///////////////////////////////////////
    // função para ativar e desativar porta em evento determinado
    ///////////////////////////////////////
    void vaiEvento(int evento)
    {
      OSCMsgReceiveReset(); //está aqui tb para permitir resetar 
      
      if (atividadeNaPorta == false)
      {
        //Serial.print("Não há nada a realizar na Porta ");
        //Serial.println(numeroDaPorta);
      }
      else  // se evento tem agendamento nesta Porta
      {
        if (foiInicializado(evento) == false)
        {
        }
        else  // se evento será utilizado (eventoInicializado[ ])
        {
          //debug_estadoDoEventoNaPorta(evento);
        
          if (estaAtivado(evento) == false)  // se evento está inativo
          {
            if (
                 (millis() - millis_inicial) >= tempoInicial[evento - 1] 
                  && 
                 (millis() - millis_inicial) <= tempoFinal[evento - 1]
               )
            {
              portas.on(numeroDaPorta - 1);
              setarEventoAtivo(evento, true);
              debug_ativaPorta();
            }
          }
          else  // se a porta já está ativada ( eventoAtivo[evento - 1] == true )
          {
            if ( (millis() - millis_inicial) >= tempoFinal[evento - 1] )
            {
              portas.off(numeroDaPorta - 1);
              debug_desativaPorta();
              setarEventoAtivo(evento, false);
              if( temAtividade() == false )
              {
                atividadeNaPorta = false;
              }
            }
          }
        }
      }
    }


    ///////////////////////////////////////
    // função pra debug - sinaliza no console ativação de Porta
    ///////////////////////////////////////
    void debug_ativaPorta()
    {
      Serial.println();
      Serial.print("ascendeu porta ");
      Serial.print(numeroDaPorta);
      Serial.print(" no tempo de: ");
      Serial.println(millis() - millis_inicial);
      Serial.println();
    }


    ///////////////////////////////////////
    // função pra debug - sinaliza no console desativação de Porta
    ///////////////////////////////////////
    void debug_desativaPorta()
    {
      Serial.println();
      Serial.print("apagou porta ");
      Serial.print(numeroDaPorta);
      Serial.print(" no tempo de: ");
      Serial.println(millis() - millis_inicial);
      Serial.println();
    }


    ///////////////////////////////////////
    // função de debug - imprime na console array eventoAtivo
    ///////////////////////////////////////
    void debug_eventoAtivo()
    {
      Serial.print("Array eventoAtivo = { ");
      Serial.print(eventoAtivo[0]);
      Serial.print(", ");
      Serial.print(eventoAtivo[1]);
      Serial.print(", ");
      Serial.print(eventoAtivo[2]);
      Serial.print(", ");
      Serial.print(eventoAtivo[3]);
      Serial.print(", ");
      Serial.print(eventoAtivo[4]);
      Serial.print(", ");
      Serial.print(eventoAtivo[5]);
      Serial.print(", ");
      Serial.print(eventoAtivo[6]);
      Serial.println(" }");
    }


    ///////////////////////////////////////
    // função de debug - imprime na console array eventoInicializado
    ///////////////////////////////////////
    void debug_eventoInicializado()
    {
      Serial.print("Array eventoInicializado = { ");
      Serial.print(eventoInicializado[0]);
      Serial.print(", ");
      Serial.print(eventoInicializado[1]);
      Serial.print(", ");
      Serial.print(eventoInicializado[2]);
      Serial.print(", ");
      Serial.print(eventoInicializado[3]);
      Serial.print(", ");
      Serial.print(eventoInicializado[4]);
      Serial.print(", ");
      Serial.print(eventoInicializado[5]);
      Serial.print(", ");
      Serial.print(eventoInicializado[6]);
      Serial.println(" }");
    }


    ///////////////////////////////////////
    // função pra debug - sinaliza no console estado de evento na Porta
    ///////////////////////////////////////
    void debug_estadoDoEventoNaPorta(int evento)
    {
      Serial.println();
      Serial.print("Estado do evento ");
      Serial.print(evento);
      Serial.print(" na Porta ");
      Serial.print(numeroDaPorta);
      Serial.println(": ");
      debug_eventoInicializado();
      debug_eventoAtivo();
    }

};


// Inicialização dos objetos para cada Porta
Porta porta1(1); Porta porta2(2); Porta porta3(3); Porta porta4(4);
Porta porta5(5); Porta porta6(6); Porta porta7(7); Porta porta8(8);
Porta porta9(9); Porta porta10(10); Porta porta11(11); Porta porta12(12);


// Inicializando Pino de entrada para sicronização com Sensor Externo
int pinoStart = 69;  // porta A15 = pin 69 | entrada de 2.4V a 5.5V
int portaSync = 0;  // armazena  valor da leitura do 


//=======================================================================================
// Configuração do programa
//=======================================================================================

void setup()
{
  pinMode(pinoStart, INPUT_PULLUP);  //configura pinoStart como ENTRADA
   
  Serial.begin(115200);

  Serial.println("OSC test");

  Ethernet.begin(mac, ip);    // Inicialização da interface ethernet

  imprimeIP();    // função imprime IP da interface no monitor serial

  rele_tensao.offAll();    // todas as portas para 12V

  Udp.begin(inPort);    // inicializa a conexão UDP de entrada
/*
  porta1.setarTempoInicial(1, 2200);
  porta2.setarTempoInicial(1, 1000);
  porta3.setarTempoInicial(1, 200);
  porta4.setarTempoInicial(1, 800);
  porta5.setarTempoInicial(1, 1800);
  porta6.setarTempoInicial(1, 2600);

  porta1.setarTempoFinal(1, 4000);
  porta2.setarTempoFinal(1, 1900);
  porta3.setarTempoFinal(1, 1200);
  porta4.setarTempoFinal(1, 5000);
  porta5.setarTempoFinal(1, 3400);
  porta6.setarTempoFinal(1, 4000);
*/
}

//=======================================================================================
// Loop do programa
//=======================================================================================
void loop()
{
  OSCMsgReceive();    // processa mensagens OSC recebida

  //sincronizacaoPortaSync();
  
  if (podeIr == true)
  {
    vaiEventos();
    //debuga_vaiEventos();
    
    verificaFimDaOperacao();
  }
}

//=======================================================================================
//          -----------------------  fim do loop  -----------------------------
//=======================================================================================


         /******************************************************************\
          *                      Funções do Programa                       *
         \******************************************************************/

//=======================================================================================
// Função para resetar Arduino
//=======================================================================================
// void (*funcaoReset) () = 0;


//=======================================================================================
// Função para permitir prosseguimento da operação em iniciaOperacao() após
//  pulso de 2.4V a 5.5V na porta de Sincronização
//
//  (Não Funciona... Tem travado o funcionamento do Arduino) (31-03-2020)
//
//=======================================================================================
void sincronizacaoPortaSync()
{
  unsigned long duration = 0;
  duration = pulseIn(pinoStart, HIGH);

  if (duration > 5) // se tiver 3.3V na porta de acionamento (pinoStart)
    {
      duration = pulseIn(pinoStart, HIGH);
      portaSync = 1;
    }
  else
    { 
      portaSync = 0;
    }
  Serial.print("NormaeHardware, sincronizacaoPortaSync(): portaSync = "); Serial.println(portaSync);
  delay_millis(45);
}


//=======================================================================================
// Função de impressão do endereço IP no monitor serial
//=======================================================================================
void imprimeIP()
{
  Serial.print("Arduino IP address: ");
  for (byte thisByte = 0; thisByte < 4; thisByte++)
  {
    // print the value of each byte of the IP address:
    Serial.print(Ethernet.localIP()[thisByte], DEC);
    Serial.print("."); 
  }
  Serial.println();
}


//=======================================================================================
// Função que processa as mensagens OSC recebidas
//=======================================================================================
/*  As mensagens OSC recebidas são roteadas para funções que efetuarão as
 *   operações desejadas.
 */

void OSCMsgReceive()
{
  OSCMessage msgIN;
  int size;
  if((size = Udp.parsePacket())>0)
  {
    while(size--)
      msgIN.fill(Udp.read());
    if(!msgIN.hasError())
    {
      msgIN.route("/reset", funcaoReset);
      msgIN.route("/setar/tensao", setaTensao);
      msgIN.route("/setar/evento", setaEvento);
      msgIN.route("/setar/tempoTotal", setaTempoTotal);
      msgIN.route("/armarNormae", mensagemDeArmado);
      msgIN.route("/iniciar", iniciaOperacao);
    }
  }
}


//=======================================================================================
// Função iniciaOperacao - seta o rele a ser manipulado 
//=======================================================================================

void iniciaOperacao(OSCMessage &msg, int addrOffset)
{
  int value = msg.getInt(0);

  if ( value == 1)
  {
    OSCMessage msgOUT("/iniciar");
    Serial.println("==========================================");
    Serial.print("Recebida mensagem para iniciar operação: ");
    Serial.println(value);

    //sincronizacaoPortaSync();  //loop sendo repetido até receber um pulso de 50ms na porta de sincronização
    podeIr = 1;   // Inicia loop do programa
    
    msgOUT.add("inicializado");
    Serial.println("          OPERAÇÃO INICIADA ");
    Serial.println("==========================================");
    Udp.beginPacket(Udp.remoteIP(), outPort);
    msgOUT.send(Udp); // send the bytes
    Udp.endPacket(); // mark the end of the OSC Packet
    msgOUT.empty(); // free space occupied by message

    millis_inicial = millis();
    
    mensagemDeOperacao(1);

  }
  else  //devolve mensagem de falha
  {
    OSCMessage msgOUT("/iniciar");
    Serial.println("===============================");
    Serial.print("Mensagem inválida recebida: ");
    Serial.println(value);
    msgOUT.add(0);
    Serial.println("===============================");
    Udp.beginPacket(Udp.remoteIP(), outPort);
    msgOUT.send(Udp);
    Udp.endPacket();
    msgOUT.empty();
  }
}


//=======================================================================================
// Função mensagemDeOperacao - mensagens de comunicação de estado da controladora
//                             /operacao/1  => está em operação
//                             /operacao/0  => não está em operação
//
//       mensagem utilizada para liberar ou não o botão INICIAR na interface gráfica
//
//=======================================================================================

void mensagemDeOperacao(int operando)
{
  OSCMessage msgOUT("/operacao");

  msgOUT.add(operando);

  Udp.beginPacket(rpi_ip, outPort);
  msgOUT.send(Udp);
  Udp.endPacket();
  msgOUT.empty();
}


//=======================================================================================
// Função mensagemDeArmado - mensagens de comunicação de estado da controladora
//                             /normaeArmada/1  => está armada e pronta para operar
//                             /normaeArmada/0  => não está pronta para operar
//
//       mensagem utilizada para liberar ou não o botão ARMAR na interface gráfica
//
//    obs: Falta uma confirmação de segurança real de que está tudo ok.
//
//=======================================================================================

void mensagemDeArmado(int armado)
{
  OSCMessage msgOUT("/normaeArmada");

  msgOUT.add(1);

  Udp.beginPacket(rpi_ip, outPort);
  msgOUT.send(Udp);
  Udp.endPacket();
  msgOUT.empty();
}


//=======================================================================================
// Função setaTensao - seta a tensão do rele selecionado previamente
//                     em 12V ou 24V.
//
//
//           setar/tensao/Porta/(int (1-12))/para/(int (12/24))
//=======================================================================================

void setaTensao(OSCMessage &msg, int addrOffset)
{
  int porta = msg.getInt(1);
  int tensao = msg.getInt(3);
  
  OSCMessage msgOUT("/porta");
  msgOUT.add(porta);

  msgOUT.add("tensao");
  
  Serial.print("Tensão da Porta n.");
  Serial.print(porta);
  Serial.print(" para ");
  Serial.print(tensao);
  Serial.println("V");

  switch(tensao)
  {
    case 12 :
      rele_tensao.off(porta - 1);
      break;
    case 24 :
      rele_tensao.on(porta - 1);
      break;
    default :
      Serial.println("Mensagem inválida.");
  }

  msgOUT.add(tensao);

  Udp.beginPacket(Udp.remoteIP(), outPort);
  msgOUT.send(Udp); // send the bytes
  Udp.endPacket(); // mark the end of the OSC Packet
  msgOUT.empty(); // free space occupied by message
}


//=======================================================================================
// Função setaEvento - configura um evento e imprime na console status de configuração
//      
//    seta variáveis releAtivo, eventoAtivo, tempoInicialEvento e
//                        tempoFinalEvento
//
//msg: /setar/evento/int(numeroDoEvento)/Porta/int(numeroDaPorta)/
//      tempoInicio/String(tempoInicio)/tempoFinal/String(tempoFinal)
//=======================================================================================

void setaEvento(OSCMessage &msg, int addrOffset)
{
  unsigned long tempoInicialEvento;   // variável para setar tempo inicial de um evento
  unsigned long tempoFinalEvento;   // variável para setar tempo final de um evento  
  
  int evento = msg.getInt(0);

  int porta = msg.getInt(2);

  char dadoString[16];

  
  msg.getString(4, dadoString, 12);
  tempoInicialEvento = strtoul(dadoString, NULL, 10);
  gravaTempoInicial(evento, porta, tempoInicialEvento);

  
  msg.getString(6, dadoString, 12);
  tempoFinalEvento = strtoul(dadoString, NULL, 10);
  gravaTempoFinal(evento, porta, tempoFinalEvento);

  //debuga_setaEvento(porta, evento);
}


//=======================================================================================
// Função debuga_setaEvento
//
//=======================================================================================
void debuga_setaEvento(int porta, int evento)
{
  unsigned long valorI = 0;
  unsigned long valorF = 0;
  Serial.println();
  Serial.println("------------------------");
  Serial.println("  debuga_setaEvento() ");
  Serial.println("------------------------");
  Serial.println("Os dados recebidos foram: ");
  Serial.print("Evento: ");
  Serial.println(evento);
  Serial.print("Porta: ");
  Serial.println(porta);
  Serial.print("Tempo Inicial: ");
  valorI = retorna_tempoInicial(porta, evento);
  Serial.println(valorI);
  Serial.print("Tempo Final: ");
  valorF = retorna_tempoFinal(porta, evento);
  Serial.println(valorF);
  Serial.print("Intervalo de tempo: ");
  unsigned long intervalo_de_tempo = valorF - valorI;
  Serial.println(intervalo_de_tempo);
  Serial.println("------------------------");
  valorI = 0;
  valorF = 0;
}


//=======================================================================================
// Função retorna_tempoInicial
//
//=======================================================================================
unsigned long retorna_tempoInicial(int porta, int evento)
{
  switch (porta)
  {
    case 1:
      return porta1.retorna_tempoInicial(evento);
      break;
    case 2:
      return porta2.retorna_tempoInicial(evento);
      break;
    case 3:
      return porta3.retorna_tempoInicial(evento);
      break;
    case 4:
      return porta4.retorna_tempoInicial(evento);
      break;
    case 5:
      return porta5.retorna_tempoInicial(evento); 
      break;
    case 6:
      return porta6.retorna_tempoInicial(evento);
      break;
    case 7:
      return porta7.retorna_tempoInicial(evento);
      break;
    case 8:
      return porta8.retorna_tempoInicial(evento);
      break;
    case 9:
      return porta9.retorna_tempoInicial(evento);
      break;
    case 10:
      return porta10.retorna_tempoInicial(evento);
      break;
    case 11:
      return porta11.retorna_tempoInicial(evento);
      break;
    case 12:
      return porta12.retorna_tempoInicial(evento);
      break;
  }
}



//=======================================================================================
// Função retorna_tempoFinal
//
//=======================================================================================
unsigned long retorna_tempoFinal(int porta, int evento)
{
  switch (porta)
  {
    case 1:
      return porta1.retorna_tempoFinal(evento);
      break;
    case 2:
      return porta2.retorna_tempoFinal(evento);
      break;
    case 3:
      return porta3.retorna_tempoFinal(evento);
      break;
    case 4:
      return porta4.retorna_tempoFinal(evento);
      break;
    case 5:
      return porta5.retorna_tempoFinal(evento);
      break;
    case 6:
      return porta6.retorna_tempoFinal(evento);
      break;
    case 7:
      return porta7.retorna_tempoFinal(evento);
      break;
    case 8:
      return porta8.retorna_tempoFinal(evento);
      break;
    case 9:
      return porta9.retorna_tempoFinal(evento);
      break;
    case 10:
      return porta10.retorna_tempoFinal(evento);
      break;
    case 11:
      return porta11.retorna_tempoFinal(evento);
      break;
    case 12:
      return porta12.retorna_tempoFinal(evento);
      break;
  }
}


//=======================================================================================
// Função setaTempoTotal - configura o tempo da operação, variável tempoTotal
//                
//            (controla o fim dos loops de eventos) 
//
// msg: /setar/tempoTotal/String(tempoInicio)
//
//=======================================================================================
void setaTempoTotal(OSCMessage &msg, int addrOffset)
{
  char dadoString[16];
  
  msg.getString(0, dadoString, 12);
  tempoTotal = strtoul(dadoString, NULL, 10);

  Serial.println("=========================================");
  Serial.println("         msg setar/tempoTotal           ");
  Serial.println();  
  Serial.print("Tempo total de operação: ");
  Serial.println(tempoTotal);
  Serial.println("=========================================");
}


//=======================================================================================
// Função vaiEventos - controla eventos em todas as portas da Normae 
//=======================================================================================
void vaiEventos()
{
  for (int i = 1; i <= 7 ; i++)
  {
    porta1.vaiEvento(i);
    porta2.vaiEvento(i);
    porta3.vaiEvento(i);
    porta4.vaiEvento(i);
    porta5.vaiEvento(i);
    porta6.vaiEvento(i);
    porta7.vaiEvento(i);
    porta8.vaiEvento(i);
    porta9.vaiEvento(i);
    porta10.vaiEvento(i);
    porta11.vaiEvento(i);
    porta12.vaiEvento(i);
  }
}


//=======================================================================================
// Função debuga_vaiEventos - controla eventos em todas as portas da Normae 
//=======================================================================================
void debuga_vaiEventos()
{
  Serial.println();
  Serial.println("------------------------");
  Serial.println("  debuga_vaiEventos() ");
  Serial.println("------------------------");
  Serial.println();
  Serial.print("Tem atividade na Porta 1? ");
  Serial.println(porta1.temAtividade());
  for (int i = 0; i < 7; i++)
  {
    Serial.print("Evento ");
    Serial.print(i + 1);
    Serial.print(", tempo inicial = ");
    Serial.print(porta1.tempoInicial[i]);
    Serial.print(", tempo final = ");
    Serial.println(porta1.tempoFinal[i]);
  }
  
  Serial.println();
  Serial.print("Tem atividade na Porta 2? ");
  Serial.println(porta2.temAtividade());
  for (int i = 0; i < 7; i++)
  {
    Serial.print("Evento ");
    Serial.print(i + 1);
    Serial.print(", tempo inicial = ");
    Serial.print(porta2.tempoInicial[i]);
    Serial.print(", tempo final = ");
    Serial.println(porta2.tempoFinal[i]);
  }

  Serial.println();
  Serial.print("Tem atividade na Porta 3? ");
  Serial.println(porta3.temAtividade());
  for (int i = 0; i < 7; i++)
  {
    Serial.print("Evento ");
    Serial.print(i + 1);
    Serial.print(", tempo inicial = ");
    Serial.print(porta3.tempoInicial[i]);
    Serial.print(", tempo final = ");
    Serial.println(porta3.tempoFinal[i]);
  }

  Serial.println();
  Serial.print("Tem atividade na Porta 4? ");
  Serial.println(porta4.temAtividade());
  for (int i = 0; i < 7; i++)
  {
    Serial.print("Evento ");
    Serial.print(i + 1);
    Serial.print(", tempo inicial = ");
    Serial.print(porta4.tempoInicial[i]);
    Serial.print(", tempo final = ");
    Serial.println(porta4.tempoFinal[i]);
  }

  Serial.println();
  Serial.print("Tem atividade na Porta 5? ");
  Serial.println(porta5.temAtividade());
  for (int i = 0; i < 7; i++)
  {
    Serial.print("Evento ");
    Serial.print(i + 1);
    Serial.print(", tempo inicial = ");
    Serial.print(porta5.tempoInicial[i]);
    Serial.print(", tempo final = ");
    Serial.println(porta5.tempoFinal[i]);
  }
  
  Serial.println();
  Serial.print("Tem atividade na Porta 6? ");
  Serial.println(porta6.temAtividade());
  for (int i = 0; i < 7; i++)
  {
    Serial.print("Evento ");
    Serial.print(i + 1);
    Serial.print(", tempo inicial = ");
    Serial.print(porta6.tempoInicial[i]);
    Serial.print(", tempo final = ");
    Serial.println(porta6.tempoFinal[i]);
  }
  
  Serial.println();
  Serial.print("Tem atividade na Porta 7? ");
  Serial.println(porta7.temAtividade());
  for (int i = 0; i < 7; i++)
  {
    Serial.print("Evento ");
    Serial.print(i + 1);
    Serial.print(", tempo inicial = ");
    Serial.print(porta7.tempoInicial[i]);
    Serial.print(", tempo final = ");
    Serial.println(porta7.tempoFinal[i]);
  }
  
  Serial.println();
  Serial.print("Tem atividade na Porta 8? ");
  Serial.println(porta8.temAtividade());
  for (int i = 0; i < 7; i++)
  {
    Serial.print("Evento ");
    Serial.print(i + 1);
    Serial.print(", tempo inicial = ");
    Serial.print(porta8.tempoInicial[i]);
    Serial.print(", tempo final = ");
    Serial.println(porta8.tempoFinal[i]);
  }
  
  Serial.println();
  Serial.print("Tem atividade na Porta 9? ");
  Serial.println(porta9.temAtividade());
  for (int i = 0; i < 7; i++)
  {
    Serial.print("Evento ");
    Serial.print(i + 1);
    Serial.print(", tempo inicial = ");
    Serial.print(porta9.tempoInicial[i]);
    Serial.print(", tempo final = ");
    Serial.println(porta9.tempoFinal[i]);
  }
  
  Serial.println();
  Serial.print("Tem atividade na Porta 10? ");
  Serial.println(porta10.temAtividade());
  for (int i = 0; i < 7; i++)
  {
    Serial.print("Evento ");
    Serial.print(i + 1);
    Serial.print(", tempo inicial = ");
    Serial.print(porta10.tempoInicial[i]);
    Serial.print(", tempo final = ");
    Serial.println(porta10.tempoFinal[i]);
  }
  
  Serial.println();
  Serial.print("Tem atividade na Porta 11? ");
  Serial.println(porta11.temAtividade());
  for (int i = 0; i < 7; i++)
  {
    Serial.print("Evento ");
    Serial.print(i + 1);
    Serial.print(", tempo inicial = ");
    Serial.print(porta11.tempoInicial[i]);
    Serial.print(", tempo final = ");
    Serial.println(porta11.tempoFinal[i]);
  }
  
  Serial.println();
  Serial.print("Tem atividade na Porta 12? ");
  Serial.println(porta12.temAtividade());
  for (int i = 0; i < 7; i++)
  {
    Serial.print("Evento ");
    Serial.print(i + 1);
    Serial.print(", tempo inicial = ");
    Serial.print(porta12.tempoInicial[i]);
    Serial.print(", tempo final = ");
    Serial.println(porta12.tempoFinal[i]);
  }
}


//=======================================================================================
// Função gravaTempoInicial - grava tempo inicial de cada evento na porta correspondente
//
//=======================================================================================
void gravaTempoInicial(int evento, int porta, unsigned long tempo)
{
  switch (porta)
  {
    case 1:
      porta1.setarTempoInicial(evento, tempo);
      break;
    case 2:
      porta2.setarTempoInicial(evento, tempo);
      break;
    case 3:
      porta3.setarTempoInicial(evento, tempo);
      break;
    case 4:
      porta4.setarTempoInicial(evento, tempo);
      break;
    case 5:
      porta5.setarTempoInicial(evento, tempo);
      break;
    case 6:
      porta6.setarTempoInicial(evento, tempo);
      break;
    case 7:
      porta7.setarTempoInicial(evento, tempo);
      break;
    case 8:
      porta8.setarTempoInicial(evento, tempo);
      break;
    case 9:
      porta9.setarTempoInicial(evento, tempo);
      break;
    case 10:
      porta10.setarTempoInicial(evento, tempo);
      break;
    case 11:
      porta11.setarTempoInicial(evento, tempo);
      break;
    case 12:
      porta12.setarTempoInicial(evento, tempo);
      break;
  }
}


//=======================================================================================
// Função gravaTempoFinal - grava tempo final de cada evento na porta correspondente
//
//=======================================================================================
void gravaTempoFinal(int evento, int porta, unsigned long tempo)
{
  switch (porta)
  {
    case 1:
      porta1.setarTempoFinal(evento, tempo);
      break;
    case 2:
      porta2.setarTempoFinal(evento, tempo);
      break;
    case 3:
      porta3.setarTempoFinal(evento, tempo);
      break;
    case 4:
      porta4.setarTempoFinal(evento, tempo);
      break;
    case 5:
      porta5.setarTempoFinal(evento, tempo);
      break;
    case 6:
      porta6.setarTempoFinal(evento, tempo);
      break;
    case 7:
      porta7.setarTempoFinal(evento, tempo);
      break;
    case 8:
      porta8.setarTempoFinal(evento, tempo);
      break;
    case 9:
      porta9.setarTempoFinal(evento, tempo);
      break;
    case 10:
      porta10.setarTempoFinal(evento, tempo);
      break;
    case 11:
      porta11.setarTempoFinal(evento, tempo);
      break;
    case 12:
      porta12.setarTempoFinal(evento, tempo);
      break;
  }
}


//=======================================================================================
// Função verificaFimDaOperacao - Interrompe Loop do Programa
//
//=======================================================================================
void verificaFimDaOperacao()
{
  if ( (millis() - millis_inicial) > (tempoTotal + 100UL) )
  {
    if (podeIr == true)
    {
      Serial.println(":)    :)    :)    :)    :)    :)    :)");
      Serial.println("          Fim da Operação");
      Serial.println(":)    :)    :)    :)    :)    :)    :)");
      Serial.println();

      mensagemDeOperacao(0);
    }
    podeIr = false;
  }
}


//-------------------------------------------------------------------------------
// Função delay_millis() - Substitui função delay().
//                          Evita que a CPU seja monopolizada durante o tempo
//                          de delay ocasionando travamentos no ESP8266.
//-------------------------------------------------------------------------------
boolean delay_millis(unsigned long intervaloTempo)
{
  unsigned long tempoInicial = millis();

  while (millis() <= tempoInicial + intervaloTempo)
  { 
    yield();
  }
  
  return true;
}

 
//=======================================================================================
//           ---------------------- fim do programa --------------------------           
//=======================================================================================

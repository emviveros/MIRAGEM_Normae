import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import controlP5.*; 
import netP5.*; 
import oscP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class interfacePC_Normae_v0_9_1 extends PApplet {

/********************************************************************************************
 * Protótipo de Interface para Normae_12 - Shiyozi
 * Interface de 1 range slider com seletor de porta para acionamento
 * e desacionamento de módulo de relé
 *
 *******************************************************************************************/

/* ToDo
 *    - Remover todos os Range Sliders e subtituir por 2Sliders com transparências pra
 *        visualização do intervalo
 *
 */


//===========================================================================================
//                            Declaração de Bibliotecas
//===========================================================================================
            //biblioteca de eLementos da interface
                //dependência para oscP5
                //biblioteca de protocolo OSC


//===========================================================================================
//                            Inicialização de objetos
//===========================================================================================
OscP5 oscP5;    //referente a biblioteca para comunicação OSC
NetAddress enderecoNormae;    //para inicialização de endereço de rede da controladora Normae
//NetAddress enderecoMac;    //para inicialização de endereço de rede do computador para debug

ControlP5 cp5;    //referente a biblioteca controlP5

Textlabel tituloPortas;    //texto com título do seletor de porta de saída

Textlabel tituloTensao;    //texto com título do seletor de tensão da porta selecionada

Textlabel indicePortas;    //texto com o número de cada porta para seletor de portas de saída

RadioButton portas;    //objeto gráfico de seleção de porta de saída

Numberbox inTempoMaximo;     //objeto gráfico de entrada de tempo máximo
Knob knobTempoMaximo;    //objeto gráfico de entrada de tempo máximo

Range rangeEvento1;    //objeto gráfico de entrada de tempo inicial e final de cada evento

Sentinela sentinela;    //objeto que armazena dados de alguns elementos da interface


//===========================================================================================
//                               Variáveis Globais
//===========================================================================================
int myColorBackground = 100;    //cor de fundo da tela de interface gráfica

boolean[] toggleValue = new boolean[12];    //seletor de tensão da porta (12V-24V)

float[] evento1Min = new float[12]; float[] evento2Min = new float[12];
float[] evento3Min = new float[12]; float[] evento4Min = new float[12];
float[] evento5Min = new float[12];

float[] evento1Max = new float[12]; float[] evento2Max = new float[12];
float[] evento3Max = new float[12]; float[] evento4Max = new float[12];
float[] evento5Max = new float[12];

float tempoTotal;

float rangeMin = 0.0f;    //valor mínimo de cada slider
float rangeMax = 5000.00f;    //valor máximo de cada slider

int porta1 = 0, porta2 = 0, porta3 = 0, porta4 = 0,    //contém o estado do botões
    porta5 = 0, porta6 = 0, porta7 = 0, porta8 = 0,    //das  Portas de Saída.
    porta9 = 0, porta10 = 0, porta11 = 0, porta12 = 0;
    
int portaAtiva;
int toogleTensaoPosX = 60;    //posição X da interface de seleção de Porta de Saída
int toogleTensaoPosY = 95;    //posição Y da interface de seleção de Porta de Saída

int col;  // variável que guarda cor de fundo p/ botão a ser ativado/desativado

//Textarea Output;
//Println console;

boolean armado = false;    // armazena estado do botão ARMAR |true armado | false desarmado

//===========================================================================================
//                       Configuração da Interface (Geração)
//===========================================================================================
public void setup()
{
  
  noStroke();
  
  surface.setTitle("MIRAGEM - Normae v0.9-1");
  surface.setResizable(true);
  surface.setLocation(5, 3);

  sentinela = new Sentinela(0, 0, 0);
  
  oscP5 = new OscP5(this,7777);    //ouvindo na porta 7777
  enderecoNormae = new NetAddress("169.254.68.51",5555);//endereço comunicação LAN com Normae
  //enderecoMac =  new NetAddress("192.168.0.200",5555);//endereço p teste mac
  
  cp5 = new ControlP5(this);
 
  gerarInterfaceAba_E1();
  
  println("--------------------------------------------------------------------------------------");
  println("                   MIRAGEM EFEITOS: Normae v0.9-1 jun2020");
  println("--------------------------------------------------------------------------------------");
  println();
  println("Interface: Inicializando Normae"); println();
  
  gerarInterfacePortas("Porta1", 1);
  gerarInterfacePortas("Porta2", 2);
  gerarInterfacePortas("Porta3", 3);
  gerarInterfacePortas("Porta4", 4);
  gerarInterfacePortas("Porta5", 5);
  gerarInterfacePortas("Porta6", 6);
  gerarInterfacePortas("Porta7", 7);
  gerarInterfacePortas("Porta8", 8);
  /*gerarInterfacePortas("Porta9", 9);
  gerarInterfacePortas("Porta10", 10);
  gerarInterfacePortas("Porta11", 11);
  gerarInterfacePortas("Porta12", 12);
  
  cp5.remove("tensao_Porta9");
  Textlabel tensao = cp5.get(Textlabel.class, "labelTensao_Porta9");
  tensao.setValue("TENSÃO 24V");
  
  cp5.remove("tensao_Porta10");
  tensao = cp5.get(Textlabel.class, "labelTensao_Porta10");
  tensao.setValue("TENSÃO 24V");
  
  cp5.remove("tensao_Porta11");
  tensao = cp5.get(Textlabel.class, "labelTensao_Porta11");
  tensao.setValue("TENSÃO 24V");
  
  cp5.remove("tensao_Porta12");
  tensao = cp5.get(Textlabel.class, "labelTensao_Porta12");
  tensao.setValue("TENSÃO 24V"); */
  
  gerarTabs();
  
  col = cp5.getController("armar_default").getColor().getBackground();  //armazena cor de background
  
  estadoBotaoIniciar(false);
  
  println("--------------------------------------------------------------------------------------");
  println("                                                                 Normae: v0.9 jun2020");
  println("--------------------------------------------------------------------------------------");
  println("                                  MIRAGEM EFEITOS");
  println("--------------------------------------------------------------------------------------");
}


//===========================================================================================
//                                   Programa
//===========================================================================================
public void draw()
{
  background(60);
}


//===========================================================================================
//                           Construção de mensagens OSC
//===========================================================================================

  //////////////////////////////////////////////////////////////////////////////////////
 /// Envia vetores de tempo mínimo e máximo de EVENTOS e INÍCIO da operação por OSC ///
//////////////////////////////////////////////////////////////////////////////////////

//msg: /setar/evento/int(numeroDoEvento)/Porta/int(numeroDaPorta)/
//      tempoInicio/String(tempoInicio)/tempoFinal/String(tempoFinal)

public void setarEventos()    // mensagens p/ setar Evento em todas as Portas
{
  setarTempoTotal_e_AtivacaoDeEventos();
  
  String tempoInicio;
  String tempoFinal;
  
    for ( int numeroDoEvento = 1; numeroDoEvento <= 7; numeroDoEvento++)
    {
      for ( int i = 0; i <= 11; i++)
      {
      OscMessage setandoEvento = new OscMessage("/setar/evento");
      setandoEvento.add(numeroDoEvento);
      setandoEvento.add("Porta");
      setandoEvento.add(i + 1);                   // porta a ser setada
        switch(numeroDoEvento)
        {
          case 1:
          setandoEvento.add("tempoInicio");
          tempoInicio = str(evento1Min[i]);   // tempo de inicio do evento
          setandoEvento.add(tempoInicio);
          setandoEvento.add("tempoFinal");
          tempoFinal = str(evento1Max[i]);    // tempo final do evento
          setandoEvento.add(tempoFinal);
          break;
          
        case 2:
          setandoEvento.add("tempoInicio");
          tempoInicio = str(evento2Min[i]);
          setandoEvento.add(tempoInicio);
          setandoEvento.add("tempoFinal");
          tempoFinal = str(evento2Max[i]);
          setandoEvento.add(tempoFinal);
          break;
          
        case 3:
          setandoEvento.add("tempoInicio");
          tempoInicio = str(evento3Min[i]);
          setandoEvento.add(tempoInicio);
          setandoEvento.add("tempoFinal");
          tempoFinal = str(evento3Max[i]);
          setandoEvento.add(tempoFinal);
          break;
          
        case 4:
          setandoEvento.add("tempoInicio");
          tempoInicio = str(evento4Min[i]);
          setandoEvento.add(tempoInicio);
          setandoEvento.add("tempoFinal");
          tempoFinal = str(evento4Max[i]);
          setandoEvento.add(tempoFinal);
          break;
          
        case 5:
          setandoEvento.add("tempoInicio");
          tempoInicio = str(evento5Min[i]);
          setandoEvento.add(tempoInicio);
          setandoEvento.add("tempoFinal");
          tempoFinal = str(evento5Max[i]);
          setandoEvento.add(tempoFinal);
          break;
        } 
      oscP5.send(setandoEvento, enderecoNormae);
      //oscP5.send(setandoEvento, enderecoMac);
      delay(20);
      }
    }
}


  ///////////////////////////////////////////////////////////////////////////
 /// Envia dado de tempo total e ativação de Eventos da operação por OSC ///
///////////////////////////////////////////////////////////////////////////

//msg: /setar/tempoTotal/String(tempoTotal)

public void setarTempoTotal_e_AtivacaoDeEventos()    // mensagens p/ setar tempoTotal
{                                             //  e Ativação de Eventos
  String tempoMaximo;
  float[] maximoMesmo = new float[5];
  
  OscMessage setandoTempoTotal = new OscMessage("/setar/tempoTotal");
  maximoMesmo[0] = max(evento1Max);
  maximoMesmo[1] = max(evento2Max);
  maximoMesmo[2] = max(evento3Max);
  maximoMesmo[3] = max(evento4Max);
  maximoMesmo[4] = max(evento5Max);
  tempoTotal = max(maximoMesmo);
  tempoMaximo = str(tempoTotal);
  
  setandoTempoTotal.add(tempoMaximo);
  oscP5.send(setandoTempoTotal, enderecoNormae);
  //oscP5.send(setandoTempoTotal, enderecoMac);
  
  for ( int i = 0; i <= 4; i++)
  {
    OscMessage setandoEventos = new OscMessage("/setar/modoAtivo");
    if ( maximoMesmo[i] == 0)
    {
      setandoEventos.add(0);
    }
    else
    {
      setandoEventos.add(1);
    }
    setandoEventos.add("evento");
    setandoEventos.add(i + 1);
    oscP5.send(setandoEventos, enderecoNormae);
    //oscP5.send(setandoEventos, enderecoMac);
  }
}


  /////////////////////////////////////////////////////////////////////////
 /// Envia mensagem de Iniciar Operação via teclado (barra de espaço) ///
/////////////////////////////////////////////////////////////////////////
public void keyPressed()
{
  switch(key)
  { 
    case (' '): 
      if(armado)
        { iniciarOperacao(); }
    
    //case('1'): interromperOperacao();
    
    case ('a'):
    { armarOperacao(); }
    
    case ('A'):
    { armarOperacao(); }
  }
  
  //if(key=='1')
  //{
  //  estadoBotaoIniciar(true);
  //  estadoBotaoArmar(true);
  //}
  //else if(key=='2')
  //{
  //  estadoBotaoIniciar(false);
  //  estadoBotaoArmar(false);
  //}
}


  //////////////////////////////////////////////////
 /// Envia mensagem de Iniciar Operação via OSC ///
//////////////////////////////////////////////////

//msg: /iniciar/int(1 para começar)

public void iniciarOperacao()
{
  //setarEventos();    //manda OSC bundle com todas as mensagens para setar os Eventos  
  OscMessage iniciarOperacao = new OscMessage("/iniciar");
  iniciarOperacao.add(1);
  oscP5.send(iniciarOperacao, enderecoNormae);
  //oscP5.send(iniciarOperacao, enderecoMac);
  
  estadoBotaoArmar(false);
}


  /////////////////////////////////////////////////
 /// Envia mensagem de Armar operação via OSC  ///
/// seta os parametros de operação no Arduino ///
////////////////////////////////////////////////

public void armarOperacao()
{
  setarEventos();    //manda OSC bundle com todas as mensagens para setar os Eventos  
  //oscP5.send(iniciarOperacao, enderecoMac);
  
  OscMessage armarNormae = new OscMessage("/armarNormae");
  armarNormae.add(1);
  oscP5.send(armarNormae, enderecoNormae);
  armado = true;
}


  ///////////////////////////////////////////////////////
 /// Envia mensagem de Interromper operação via OSC  ///
///////////////////////////////////////////////////////

public void interromperOperacao()
{
  OscMessage reset = new OscMessage("/reset");
  reset.add(1);
  oscP5.send(reset, enderecoNormae);
}


  /////////////////////////////////////////////////////////////
 /// Envia controle de tensão dos Reles nas Portas via OSC ///
/////////////////////////////////////////////////////////////

//msg: /setar/tensao/Porta/(int (1-12))/para/(boolean (true/false))

public void setarTensaoPorta(int numeroDaPorta, boolean estadoDoToggle)
{
  OscMessage setarTensao = new OscMessage("/setar/tensao");
  if( estadoDoToggle == true)
  {
    print("Interface:    Enviando mensagem para setar Porta " + numeroDaPorta + " para 12V");
    println();
    setarTensao.add("Porta");
    setarTensao.add(numeroDaPorta);
    setarTensao.add("para");
    setarTensao.add(12);
    oscP5.send(setarTensao, enderecoNormae);
    //oscP5.send(setarTensao, enderecoMac);
  }
  else if ( estadoDoToggle == false )
  {
    print("Interface:    Enviando mensagem para setar Porta " + numeroDaPorta + " para 24V");
    println();
    setarTensao.add("Porta");
    setarTensao.add(numeroDaPorta);
    setarTensao.add("para");
    setarTensao.add(24);
    oscP5.send(setarTensao, enderecoNormae);
    //oscP5.send(setarTensao, enderecoMac);
  }
}


//===========================================================================================
//                           Recebimento de mensagens OSC
//===========================================================================================

/* incoming osc message are forwarded to the oscEvent method. */
public void oscEvent(OscMessage theOscMessage)
{
  //  /* print the address pattern and the typetag of the received OscMessage */
  //print("### received an osc message.");
  //print(" addrpattern: "+theOscMessage.addrPattern());
  //println(" typetag: "+theOscMessage.typetag());
  
  
  
   //////////////////////////////////////////////////////////////
  // Mensagens OSC recebidas por estado da operação de disparos
  
  if (theOscMessage.addrPattern().equals("/operacao"))
  {
    if (theOscMessage.get(0).intValue() == 1)
    {
      println("--------------------------------------------------------------------------------------");
      delay(200); println("Normae operando");
      estadoBotaoArmar(false);
      estadoBotaoIniciar(false);
      println("...");
      delay(400);
    }
    else
    {
      cp5.getTab("Porta1").bringToFront();
      println();  delay(280);
      println("Normae terminou"); delay(280);
      println("--------------------------------------------------------------------------------------");
      delay(280);
      estadoBotaoArmar(true);
      estadoBotaoIniciar(false);
      cp5.getTab("default").bringToFront();
      println("--------------------------------------------------------------------------------------");
      delay(300);
    }
  }
  
  
   ///////////////////////////////////////////////////////////
  // Mensagens OSC recebidas por seleção de tensão das portas
  
  if (theOscMessage.addrPattern().equals("/porta"))
  {
    if (theOscMessage.get(0).intValue() == 12)
    {
      println("Normae:    Porta ", theOscMessage.get(0).intValue(), "setada para ",
              theOscMessage.get(1).intValue(), "V \n");
    }
    else 
    {
      println("Normae:    Porta ", theOscMessage.get(0).intValue(), "setada para ",
              theOscMessage.get(2).intValue(), "V \n");
    }
  }
  
  
   //////////////////////////////////////////////////////
  // Mensagens OSC recebidas por Armar Normae para ação
  
  if (theOscMessage.addrPattern().equals("/normaeArmada"))
  {
    if (theOscMessage.get(0).intValue() == 1)
    {
      estadoBotaoIniciar(true);
      println("Normae:    Tudo pronto para o início.");
      println("Normae:    ---  Pressione a tecla de ESPACO para inicio.  --- \n"); delay(300);
      println("--------------------------------------------------------------------------------------");
      delay(280);
      
    }
    else 
    { 
      estadoBotaoIniciar(false);
      println();
      println("Normae:    Algo deu errado ao armar a Normae.");  delay(300);
      println();
    }
  }
}


//===========================================================================================
//               Controle de Eventos dos Elementos da Interface
//===========================================================================================

public void controlEvent(ControlEvent theEvent)
{
     //////////////////////////////////
   //  Gerenciando botões de ARMAR  //
  ///////////////////////////////////

  if(theEvent.isFrom("armar_default"))    //responde ao acionar botão ARMAR
  {
    println("Interface:    Enviando eventos aba E1 "); delay(400);
    armarOperacao();
  }
  
    /////////////////////////////////////
   //  Gerenciando botões de INICIAR  //
  /////////////////////////////////////

  if(theEvent.isFrom("iniciar_default"))    //responde ao acionar botão INICIAR
  {
    println("Interface:    Enviando eventos aba E1"); delay(1200);
    iniciarOperacao();
  }
  
  else if(theEvent.isFrom("iniciar_Porta1"))
  {
    println("Interface:    Enviando eventos aba Porta 1");
    iniciarOperacao();
  }
  else if(theEvent.isFrom("iniciar_Porta2"))
  {
    println("Interface:    Enviando eventos aba Porta 2");
    iniciarOperacao();
  }
  else if(theEvent.isFrom("iniciar_Porta3"))
  {
    println("Interface:    Enviando eventos aba Porta 3");
    iniciarOperacao();
  }
  else if(theEvent.isFrom("iniciar_Porta4"))
  {
    println("Interface:    Enviando eventos aba Porta 4");
    iniciarOperacao();
  }
  else if(theEvent.isFrom("iniciar_Porta5"))
  {
    println("Interface:    Enviando eventos aba Porta 5");
    iniciarOperacao();
  }
  else if(theEvent.isFrom("iniciar_Porta6"))
  {
    println("Interface:    Enviando eventos aba Porta 6");
    iniciarOperacao();
  }
  else if(theEvent.isFrom("iniciar_Porta7"))
  {
    println("Interface:    Enviando eventos aba Porta 7");
    iniciarOperacao();
  }
  else if(theEvent.isFrom("iniciar_Porta8"))
  {
    println("Interface:    Enviando eventos aba Porta 8");
    iniciarOperacao();
  }
  else if(theEvent.isFrom("iniciar_Porta9"))
  {
    println("Interface:    Enviando eventos aba Porta 9");
    iniciarOperacao();
  }
  else if(theEvent.isFrom("iniciar_Porta10"))
  {
    println("Interface:    Enviando eventos aba Porta 10");
    iniciarOperacao();
  }
  else if(theEvent.isFrom("iniciar_Porta11"))
  {
    println("Interface:    Enviando eventos aba Porta 11");
    iniciarOperacao();
  }
  else if(theEvent.isFrom("iniciar_Porta12"))
  {
    println("Interface:    Enviando eventos aba Porta 12");
    iniciarOperacao();
  }


    /////////////////////////////////
   //  Gerenciando Numberbox Min  //
  /*////////////////////////////////
  else if(theEvent.isFrom("numMiniE1_Porta1"))
  {
    float tempo = theEvent.getValue();
    String nome = theEvent.getName();
    sentinela.dedurarNumberbox(nome, tempo);
    setarVetorSaida(theEvent.getController().isMousePressed());
    setarValoresNumberbox(theEvent.getController().isMousePressed());
  }
  
    /////////////////////////////////
   //  Gerenciando Numberbox Max  //
  /////////////////////////////////
  
  if(theEvent.isFrom("numMaxiE1_Porta1"))
  {
    float tempo = theEvent.getValue();
    String nome = theEvent.getName();
    sentinela.dedurarNumberbox(nome, tempo);
    setarVetorSaida(theEvent.getController().isMousePressed());
    setarValoresNumberbox(theEvent.getController().isMousePressed());
  }
  
    ///////////////////////////////////////
   //  Gerenciando Numberbox Intervalo  //
  ///////////////////////////////////////
  if(theEvent.isFrom("numIntvE1_Porta1"))
  {
    float tempo = theEvent.getValue();
    String nome = theEvent.getName();
    sentinela.dedurarNumberbox(nome, tempo);
    setarVetorSaida(theEvent.getController().isMousePressed());
    setarValoresNumberbox(theEvent.getController().isMousePressed());
  }
*/
}


// Função para setar valores máx e min dos Numberbox dos custom rangeSliders (gambiarra)
public void valoresRangeSlider(String nome, float minimo, float maximo)
{
  int min = PApplet.parseInt(minimo);
  int max = PApplet.parseInt(maximo);
  int inter = PApplet.parseInt(maximo - minimo);
  
  Numberbox valorMin = cp5.get(Numberbox.class, "numMini" + nome);
  valorMin.setValue(min);
  
  Numberbox valorMax = cp5.get(Numberbox.class, "numMax" + nome);
  valorMax.setValue(max);
  
  Numberbox intervalo = cp5.get(Numberbox.class, "numIntervalo" + nome);
  intervalo.setValue(inter);
  
}


// Função que seta os valores máx e min de eventos nos vetores de saída
public void setaVetoresEvento(int evento, int porta, float tempoInicial, float tempoFinal)
{
  switch(evento)
  {
    case 1:
      evento1Min[porta - 1] = tempoInicial;
      evento1Max[porta - 1] = tempoFinal;
      break;
      
    case 2:
      evento2Min[porta - 1] = tempoInicial;
      evento2Max[porta - 1] = tempoFinal;
      break;
      
    case 3:
      evento3Min[porta - 1] = tempoInicial;
      evento3Max[porta - 1] = tempoFinal;
      break;
      
    case 4:
      evento4Min[porta - 1] = tempoInicial;
      evento4Max[porta - 1] = tempoFinal;
      break;
      
    case 5:
      evento5Min[porta - 1] = tempoInicial;
      evento5Max[porta - 1] = tempoFinal;
      break;
  }  
}


// Função de debug para Eventos (interface)
public void debugInterfaceEventos(int evento, int porta)
{
  println("=====  DEBUG IntefaceEventos COMEÇO =====");
  print("Mínimo e máximo do Evento "+ evento + " Porta " + porta + ": ");
  
  switch (evento)
  {
    case 1:
      println("(" + evento1Min[porta - 1] + ", " + evento1Max[porta - 1] + ")");
      break;
    
    case 2:
      println("(" + evento2Min[porta - 1] + ", " + evento2Max[porta - 1] + ")");
      break;
    
    case 3:
      println("(" + evento3Min[porta - 1] + ", " + evento3Max[porta - 1] + ")");
      break;
    
    case 4:
      println("(" + evento4Min[porta - 1] + ", " + evento4Max[porta - 1] + ")");
      break;
    
    case 5:
      println("(" + evento5Min[porta - 1] + ", " + evento5Max[porta - 1] + ")");
      break;
  }
  //println("=====  DEBUG IntefaceEventos F I M  =====");
}


// Função para setar valores da interface rangeSlider 
//   ( utilizado para plugar Eventos 1 de cada porta aos Eventos da aba E1 )
// futuramente deve setar interface de dois sliders em substituição do Range Slider bugado
public void updateRange(String nome, int porta, float min, float max, boolean mousepressed)
{  //preciso que após setar range, sete os valores numéricos e por último o vetor de saída!!!!
  if (mousepressed)
  {
    //println(nome + " -- - DEBUG nome em updateRange()  -- INICIO "); println();
    Range rangeSlider = cp5.get(Range.class, nome);
    rangeSlider.setLowValue(min);
    rangeSlider.setHighValue(max);    //setada interface (apenas para visual)
    
    nome = nome.substring(1);//preparar "nome" (primeiro caracter minusculo
    nome = "E" + nome;
    valoresRangeSlider(nome, min, max);  //setados os valores numéricos
    
    evento1Min[porta - 1] = min;
    evento1Max[porta - 1] = max;  //setados os tempos de inicio e fim no vetor de saída
    //print("em updateRange(): ");debugInterfaceEventos(1, 1);
    //println( nome + " -- - DEBUG nome em updateRange() --  F I M"); println();
  }
}


  ////////////////////////////////////
 //  Gerenciando toogle de TENSÃO  //
////////////////////////////////////

public void tensao_Porta1(boolean estadoToggle)
{
  //println("Interface:    Enviando mensagem de setar tensão da Porta 1");  println();
  setarTensaoPorta(1, estadoToggle);
}

public void tensao_Porta2(boolean estadoToggle)
{
  //println("Interface:    Enviando mensagem de setar tensão da Porta 2");  println();
  setarTensaoPorta(2, estadoToggle);
}

public void tensao_Porta3(boolean estadoToggle)
{
  //println("Interface:    Enviando mensagem de setar tensão da Porta 3");  println();
  setarTensaoPorta(3, estadoToggle);
}

public void tensao_Porta4(boolean estadoToggle)
{
  //println("Interface:    Enviando mensagem de setar tensão da Porta 4");  println();
  setarTensaoPorta(4, estadoToggle);
}

public void tensao_Porta5(boolean estadoToggle)
{
  //println("Interface:    Enviando mensagem de setar tensão da Porta 5");  println();
  setarTensaoPorta(5, estadoToggle);
}

public void tensao_Porta6(boolean estadoToggle)
{
  //println("Interface:    Enviando mensagem de setar tensão da Porta 6");  println();
  setarTensaoPorta(6, estadoToggle);
}

public void tensao_Porta7(boolean estadoToggle)
{
  //println("Interface:    Enviando mensagem de setar tensão da Porta 7");  println();
  setarTensaoPorta(7, estadoToggle);
}

public void tensao_Porta8(boolean estadoToggle)
{
  //println("Interface:    Enviando mensagem de setar tensão da Porta 8");  println();
  setarTensaoPorta(8, estadoToggle);
}

public void tensao_Porta9(boolean estadoToggle)
{
  //println("Interface:    Enviando mensagem de setar tensão da Porta 9");  println();
  setarTensaoPorta(9, estadoToggle);
}

public void tensao_Porta10(boolean estadoToggle)
{
  //println("Interface:    Enviando mensagem de setar tensão da Porta 10");  println();
  setarTensaoPorta(10, estadoToggle);
}

public void tensao_Porta11(boolean estadoToggle)
{
  //println("Interface:    Enviando mensagem de setar tensão da Porta 11");  println();
  setarTensaoPorta(11, estadoToggle);
}

public void tensao_Porta12(boolean estadoToggle)
{
  //println("Interface:    Enviando mensagem de setar tensão da Porta 12");  println();
  setarTensaoPorta(12, estadoToggle);
}


//===========================================================================================
//  Gerador do elemento de interface Tabs (que seleciona a Porta a ser configurada)
//
//===========================================================================================
public void gerarTabs()
{
  PFont pfont = createFont("Arial",20,true); // use true/false for smooth/no-smooth
  ControlFont font_min = new ControlFont(pfont, 12);
  
  cp5.getTab("default")
     .activateEvent(true)
     .setLabel("    E1")
     .setHeight(50)
     .setWidth(50)
     .setId(1)
     .getCaptionLabel()
     .setFont(font_min)
     ;
  
  cp5.getTab("Porta1")
     .activateEvent(true)
     .setLabel("     1")
     .setHeight(50)
     .setWidth(50)
     .setId(1)
     .getCaptionLabel()
     .setFont(font_min)
     ;
  
  cp5.addTab("Porta2")
     .activateEvent(true)
     .setLabel("     2")
     .setHeight(50)
     .setWidth(50)
     .setId(2)
     .getCaptionLabel()
     .setFont(font_min)
     ;

  cp5.addTab("Porta3")
     .activateEvent(true)
     .setLabel("     3")
     .setHeight(50)
     .setWidth(50)
     .setId(3)
     .getCaptionLabel()
     .setFont(font_min)
     ;
  
  cp5.addTab("Porta4")
     .activateEvent(true)
     .setLabel("     4")
     .setHeight(50)
     .setWidth(50)
     .setId(4)
     .getCaptionLabel()
     .setFont(font_min)
     ;

  cp5.addTab("Porta5")
     .activateEvent(true)
     .setLabel("     5")
     .setHeight(50)
     .setWidth(50)
     .setId(5)
     .getCaptionLabel()
     .setFont(font_min)
     ;

  cp5.addTab("Porta6")
     .activateEvent(true)
     .setLabel("     6")
     .setHeight(50)
     .setWidth(50)
     .setId(6)
     .getCaptionLabel()
     .setFont(font_min)
     ;

  cp5.addTab("Porta7")
     .activateEvent(true)
     .setLabel("     7")
     .setHeight(50)
     .setWidth(50)
     .setId(7)
     .getCaptionLabel()
     .setFont(font_min)
     ;

  cp5.addTab("Porta8")
     .activateEvent(true)
     .setLabel("     8")
     .setHeight(50)
     .setWidth(50)
     .setId(8)
     .getCaptionLabel()
     .setFont(font_min)
     ;
/*
  cp5.addTab("Porta9")
     .activateEvent(true)
     .setLabel("     9")
     .setHeight(50)
     .setWidth(50)
     .setId(9)
     .getCaptionLabel()
     .setFont(font_min)
     ;
  
  cp5.addTab("Porta10")
     .activateEvent(true)
     .setLabel("    10")
     .setHeight(50)
     .setWidth(50)
     .setId(10)
     .getCaptionLabel()
     .setFont(font_min)
     ;

  cp5.addTab("Porta11")
     .activateEvent(true)
     .setLabel("    11")
     .setHeight(50)
     .setWidth(50)
     .setId(11)
     .getCaptionLabel()
     .setFont(font_min)
     ;

  cp5.addTab("Porta12")
     .activateEvent(true)
     .setLabel("    12")
     .setHeight(50)
     .setWidth(50)
     .setId(12)
     .getCaptionLabel()
     .setFont(font_min)
     ; */
}


//===========================================================================================
//  gerarInterfaceAba_E1 - gera elementos da Interface para a Tab de Inicial
//
//===========================================================================================

public void gerarInterfaceAba_E1()
{
  int azulescuro = color( 0, 45, 90);
  int posX = 62;
  int posY = 90;
  String nomeNumbox = "";
  
  PFont pfont = createFont("Arial",20,true); // use true/false for smooth/no-smooth
  ControlFont font = new ControlFont(pfont, 20);
  ControlFont font_min = new ControlFont(pfont, 12);
  ControlFont font_med = new ControlFont(pfont, 16);
  
  //Output=cp5.addTextarea("Output")
  //    .setPosition(710, 162)
  //    .setSize(360, 290)
  //    .setLineHeight(16)
  //    .setFont(font_min)
  //    .setColor(color(200))
  //    .setColorBackground(color(45,45,45))
  //    .setColorForeground(color(90,90,90))
  //    .scroll(0)
  //    //.hideScrollbar();
  //    .updateAbsolutePosition();
   
  //console = cp5.addConsole(Output);

  cp5.addButton("logo")
     .setImage(loadImage("logoMiragem01.png"))
     .updateSize()
     .setPosition(700, 480)
     .setLock(true);

  cp5.addTextlabel("labelSaida_default")
     .setText("PORTAS")
     .setPosition(495, 10)
     .setColorValue(255)
     .setFont(font)
     ;
  
    cp5.addButton("armar_default")
     .setPosition(882,50)
     .setSize(82,82)
     .setLabel("armar")
     .setFont(font_med)
     .isPressed()
     ;
  cp5.getController("armar_default").moveTo("default");
  
  cp5.addButton("iniciar_default")
     .setPosition(982,50)
     .setSize(82,82)
     .setLabel("iniciar")
     .setFont(font_med)
     .isPressed()
     ;
  cp5.getController("iniciar_default").moveTo("default");

  cp5.addButton("menos_default")
     .setPosition(5900,240)
     .setSize(55,55)
     .setLabel("-")
     .setFont(font_med)
     .isPressed()
     ;
  cp5.getController("menos_default").moveTo("default");
  
  cp5.addButton("mais_default")
     .setPosition(5960,240)
     .setSize(55,55)
     .setLabel("+")
     .setFont(font_med)
     .isPressed()
     ;
  cp5.getController("mais_default").moveTo("default");
  

  //// - Range Slider EVENTO 1 da Porta 1 - ////
  int offsetY = 0;
  cp5.addRange("E1_Porta1")
     .setColorForeground(azulescuro)
     .setBroadcast(false)
     .setDecimalPrecision(0)
     .setPosition(posX, posY)
     .setSize(580, 52)
     .setHandleSize(18)
     .setRange(rangeMin, rangeMax)
     .setRangeValues(rangeMin, rangeMax)
     .setLabel("E1_P1")
     .setFont(font)
     .setId(41)
     .setLabelVisible(false)
     //.setBroadcast(true)
     .lock()
     ;
  
  cp5.addTextlabel("nomeE1_Porta1")
     .setText("PORTA 1")
     .setPosition( (posX + 230) , (posY + 6) )
     .setColorValue(255)
     .setFont(font)
     ;

nomeNumbox = "numMiniE1_Porta1";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(100, 20)
     .setPosition( (posX + 5) , (posY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numMaxiE1_Porta1";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 495) , (posY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numIntvE1_Porta1";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 260) , (posY + 30) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font_min)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );

  //// - Range Slider EVENTO 1 da Porta 2 - ////
  int valY = 75;
  offsetY = offsetY + valY;
  cp5.addRange("E1_Porta2")
     .setColorForeground(azulescuro)
     .setBroadcast(false)
     .setDecimalPrecision(0)
     .setPosition(posX, posY + offsetY)
     .setSize(580, 52)
     .setHandleSize(18)
     .setRange(rangeMin, rangeMax)
     .setRangeValues(rangeMin, rangeMax)
     .setLabel("E1_P2")
     .setFont(font)
     .setId(41)
     .setLabelVisible(false)
     //.setBroadcast(true)
     .lock()
     ;
  
  cp5.addTextlabel("nomeE1_Porta2")
     .setText("PORTA 2")
     .setPosition( (posX + 230) , (posY + offsetY + 6) )
     .setColorValue(255)
     .setFont(font)
     ;

  nomeNumbox = "numMiniE1_Porta2";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(100, 20)
     .setPosition( (posX + 5) , (posY + offsetY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numMaxiE1_Porta2";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 495) , (posY + offsetY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numIntvE1_Porta2";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 260) , (posY + offsetY + 30) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font_min)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );


  //// - Range Slider EVENTO 1 da Porta 3 - ////
  offsetY = offsetY + valY;
  cp5.addRange("E1_Porta3")
     .setColorForeground(azulescuro)
     .setBroadcast(false)
     .setDecimalPrecision(0)
     .setPosition(posX, posY + offsetY)
     .setSize(580, 52)
     .setHandleSize(18)
     .setRange(rangeMin, rangeMax)
     .setRangeValues(rangeMin, rangeMax)
     .setLabel("E1_P3")
     .setFont(font)
     .setId(41)
     .setLabelVisible(false)
     //.setBroadcast(true)
     .lock()
     ;
  
  cp5.addTextlabel("nomeE1_Porta3")
     .setText("PORTA 3")
     .setPosition( (posX + 230) , (posY + offsetY + 6) )
     .setColorValue(255)
     .setFont(font)
     ;
    
  nomeNumbox = "numMiniE1_Porta3";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(100, 20)
     .setPosition( (posX + 5) , (posY + offsetY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numMaxiE1_Porta3";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 495) , (posY + offsetY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numIntvE1_Porta3";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 260) , (posY + offsetY + 30) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font_min)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );


  //// - Range Slider EVENTO 1 da Porta 4 - ////
  offsetY = offsetY + valY;
  cp5.addRange("E1_Porta4")
     .setColorForeground(azulescuro)
     .setBroadcast(false)
     .setDecimalPrecision(0)
     .setPosition(posX, posY + offsetY)
     .setSize(580, 52)
     .setHandleSize(18)
     .setRange(rangeMin, rangeMax)
     .setRangeValues(rangeMin, rangeMax)
     .setLabel("E1_P4")
     .setFont(font)
     .setId(41)
     .setLabelVisible(false)
     //.setBroadcast(true)
     .lock()
     ;
  
  cp5.addTextlabel("nomeE1_Porta4")
     .setText("PORTA 4")
     .setPosition( (posX + 230) , (posY + offsetY + 6) )
     .setColorValue(255)
     .setFont(font)
     ;
  
  nomeNumbox = "numMiniE1_Porta4";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(100, 20)
     .setPosition( (posX + 5) , (posY + offsetY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numMaxiE1_Porta4";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 495) , (posY + offsetY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numIntvE1_Porta4";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 260) , (posY + offsetY + 30) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font_min)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );


  //// - Range Slider EVENTO 1 da Porta 5 - ////
  offsetY = offsetY + valY;
  cp5.addRange("E1_Porta5")
     .setColorForeground(azulescuro)
     .setBroadcast(false)
     .setDecimalPrecision(0)
     .setPosition(posX, posY + offsetY)
     .setSize(580, 52)
     .setHandleSize(18)
     .setRange(rangeMin, rangeMax)
     .setRangeValues(rangeMin, rangeMax)
     .setLabel("E1_P5")
     .setFont(font)
     .setId(41)
     .setLabelVisible(false)
     //.setBroadcast(true)
     .lock()
     ;
  
  cp5.addTextlabel("nomeE1_Porta5")
     .setText("PORTA 5")
     .setPosition( (posX + 230) , (posY + offsetY + 6) )
     .setColorValue(255)
     .setFont(font)
     ;
  
  nomeNumbox = "numMiniE1_Porta5";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(100, 20)
     .setPosition( (posX + 5) , (posY + offsetY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numMaxiE1_Porta5";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 495) , (posY + offsetY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numIntvE1_Porta5";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 260) , (posY + offsetY + 30) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font_min)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );


  //// - Range Slider EVENTO 1 da Porta 6 - ////
  offsetY = offsetY + valY;
  cp5.addRange("E1_Porta6")
     .setColorForeground(azulescuro)
     .setBroadcast(false)
     .setDecimalPrecision(0)
     .setPosition(posX, posY + offsetY)
     .setSize(580, 52)
     .setHandleSize(18)
     .setRange(rangeMin, rangeMax)
     .setRangeValues(rangeMin, rangeMax)
     .setLabel("E1_P6")
     .setFont(font)
     .setId(46)
     .setLabelVisible(false)
     //.setBroadcast(true)
     .lock()
     ;
  
  cp5.addTextlabel("nomeE1_Porta6")
     .setText("PORTA 6")
     .setPosition( (posX + 230) , (posY + offsetY + 6) )
     .setColorValue(255)
     .setFont(font)
     ;

  nomeNumbox = "numMiniE1_Porta6";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(100, 20)
     .setPosition( (posX + 5) , (posY + offsetY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numMaxiE1_Porta6";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 495) , (posY + offsetY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numIntvE1_Porta6";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 260) , (posY + offsetY + 30) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font_min)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );


  //// - Range Slider EVENTO 1 da Porta 7 - ////
  offsetY = offsetY + valY;
  cp5.addRange("E1_Porta7")
     .setColorForeground(azulescuro)
     .setBroadcast(false)
     .setDecimalPrecision(0)
     .setPosition(posX, posY + offsetY)
     .setSize(580, 52)
     .setHandleSize(18)
     .setRange(rangeMin, rangeMax)
     .setRangeValues(rangeMin, rangeMax)
     .setLabel("E1_P7")
     .setFont(font)
     .setId(41)
     .setLabelVisible(false)
     //.setBroadcast(true)
     .lock()
     ;
  
  cp5.addTextlabel("nomeE1_Porta7")
     .setText("PORTA 7")
     .setPosition( (posX + 230) , (posY + offsetY + 6) )
     .setColorValue(255)
     .setFont(font)
     ;

  nomeNumbox = "numMiniE1_Porta7";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(100, 20)
     .setPosition( (posX + 5) , (posY + offsetY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numMaxiE1_Porta7";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 495) , (posY + offsetY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numIntvE1_Porta7";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 260) , (posY + offsetY + 30) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font_min)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );


  // cor em hex do azul escuro 002D5A e RGB( 0, 45, 90)
  //// - Range Slider EVENTO 1 da Porta 8 - ////
  offsetY = offsetY + valY;
  cp5.addRange("E1_Porta8")
     .setColorForeground(azulescuro)
     .setBroadcast(false)
     .setDecimalPrecision(0)
     .setPosition(posX, posY + offsetY)
     .setSize(580, 52)
     .setHandleSize(18)
     .setRange(rangeMin, rangeMax)
     .setRangeValues(rangeMin, rangeMax)
     .setLabel("E1_P8")
     .setFont(font)
     .setId(41)
     .setLabelVisible(false)
     //.setBroadcast(true)
     .lock()
     ;
  
  cp5.addTextlabel("nomeE1_Porta8")
     .setText("PORTA 8")
     .setPosition( (posX + 230) , (posY + offsetY + 6) )
     .setColorValue(255)
     .setFont(font)
     ;

  nomeNumbox = "numMiniE1_Porta8";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(100, 20)
     .setPosition( (posX + 5) , (posY + offsetY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numMaxiE1_Porta8";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 495) , (posY + offsetY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numIntvE1_Porta8";
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 260) , (posY + offsetY + 30) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font_min)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
}


//===========================================================================================
//  gerarInterfacePortas - gera elementos da Interface para uma Tab de Porta
//
//===========================================================================================

public void gerarInterfacePortas(String tab, int ID)
{
  int azulescuro = color( 0, 45, 90);
  int posX = 480;      //posição x do primeiro slider
  int posY = 200;      //posição y do primeiro slider
  int posX_TempoMaximo = 9120;
  int poxY_TempoMaximo = 320;
  String nomeNumbox = "";

  PFont pfont = createFont("Arial",20,true); // use true/false for smooth/no-smooth
  ControlFont font = new ControlFont(pfont, 20);
  ControlFont font_min = new ControlFont(pfont, 12);
  ControlFont font_med = new ControlFont(pfont, 16);
  
  cp5.addTextlabel("labelSaida" + "_" + tab)
     .setText("PORTAS")
     .setPosition(495, 10)
     .setColorValue(255)
     .setFont(font)
     .moveTo(tab)
     ;

  cp5.addTextlabel("labelTensao" + "_" + tab)
     .setText("TENSÃO")
     .setPosition( (toogleTensaoPosX + 80) , toogleTensaoPosY )
     .setColorValue(255)
     .setFont(font)
     .moveTo(tab)
     ;
  
  cp5.addButton("iniciar" + "_" + tab)
     .setPosition(posX + 502 ,50)
     .setSize(82,82)
     .setLabel("iniciar")
     .setFont(font_med)
     .isPressed()
     ;
  cp5.getController("iniciar" + "_" + tab).moveTo(tab);
  
    cp5.addTextlabel("labelTempoMaximo" + "_" + tab)
     .setText("TEMPO MÁXIMO")
     .setPosition( (posX_TempoMaximo - 8) , (poxY_TempoMaximo + 160) )
     .setColorValue(255)
     .setFont(font)
     .moveTo(tab)
     ;

  cp5.addToggle("tensao" + "_" + tab)
     .setPosition( toogleTensaoPosX , toogleTensaoPosY )
     .setSize(72, 26)
     .setValue(true)
     .setMode(ControlP5.SWITCH)
     .setLabel(" 12V  -  24V")
     .setFont(font_min)
     .moveTo(tab)
     ;

//// - Range Slider EVENTO 1 - ////
  cp5.addRange("evento_1" + "_" + tab)
     .setColorForeground(azulescuro)
     .setBroadcast(false)
     .setDecimalPrecision(0)
     .setPosition(posX, posY)
     .setSize(580, 52)
     .setHandleSize(18)
     .setRange(rangeMin, rangeMax)
     .setRangeValues(rangeMin, rangeMax)
     .setLabel("Evento 1")
     .setFont(font)
     .setId(100 + ID)
     .setLabelVisible(false)
     //.setBroadcast(true)
     .lock()
     .moveTo(tab)
     ;
  
  cp5.addTextlabel("nomeEvento_1" + "_" + tab)
     .setText("EVENTO 1")
     .setPosition( (posX + 230) , (posY + 6) )
     .setColorValue(255)
     .setFont(font)
     .moveTo(tab)
     ;

  nomeNumbox = "numMiniEvento_1" + "_" + tab;
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(100, 20)
     .setPosition( (posX + 5) , (posY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     .moveTo(tab)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numMaxiEvento_1" + "_" + tab;
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 495) , (posY + 15) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     .moveTo(tab)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numIntvEvento_1" + "_" + tab;
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 260) , (posY + 30) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font_min)
     .moveTo(tab)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );


//// - Range Slider EVENTO 2 - ////
  cp5.addRange("evento_2" + "_" + tab)
     .setColorForeground(azulescuro)
     .setBroadcast(false)
     .setDecimalPrecision(0)
     .setPosition(posX, posY+80)
     .setSize(580, 52)
     .setHandleSize(18)
     .setRange(rangeMin, rangeMax)
     .setRangeValues(rangeMin, rangeMax)
     .setLabel("Evento 2")
     .setFont(font)
     .setId(200 + ID)
     .setLabelVisible(false)
     //.setBroadcast(true)
     .lock()
     .moveTo(tab)
     ;

  cp5.addTextlabel("nomeEvento_2" + "_" + tab)
     .setText("EVENTO 2")
     .setPosition( (posX + 230) , (posY + 86) )
     .setColorValue(255)
     .setFont(font)
     .moveTo(tab)
     ;

  nomeNumbox = "numMiniEvento_2" + "_" + tab;
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(100, 20)
     .setPosition( (posX + 5) , (posY + 95) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     .moveTo(tab)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numMaxiEvento_2" + "_" + tab;
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 495) , (posY + 95) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     .moveTo(tab)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numIntvEvento_2" + "_" + tab;
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 260) , (posY + 110) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font_min)
     .moveTo(tab)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );


//// - Range Slider EVENTO 3 - ////
  cp5.addRange("evento_3" + "_" + tab)
     .setColorForeground(azulescuro)
     .setBroadcast(false)
     .setDecimalPrecision(0)
     .setPosition(posX, posY+160)
     .setSize(580, 52)
     .setHandleSize(18)
     .setRange(rangeMin, rangeMax)
     .setRangeValues(rangeMin, rangeMax)
     .setLabel("Evento 3")
     .setFont(font)
     .setId(300 + ID)
     .setLabelVisible(false)
     //.setBroadcast(true)
     .lock()
     .moveTo(tab)
     ;

  cp5.addTextlabel("nomeEvento_3" + "_" + tab)
     .setText("EVENTO 3")
     .setPosition( (posX + 230) , (posY + 166) )
     .setColorValue(255)
     .setFont(font)
     .moveTo(tab)
     ;

  nomeNumbox = "numMiniEvento_3" + "_" + tab;
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(100, 20)
     .setPosition( (posX + 5) , (posY + 175) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     .moveTo(tab)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numMaxiEvento_3" + "_" + tab;
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 495) , (posY + 175) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     .moveTo(tab)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numIntvEvento_3" + "_" + tab;
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 260) , (posY + 192) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font_min)
     .moveTo(tab)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );


//// - Range Slider EVENTO 4 - ////
  cp5.addRange("evento_4" + "_" + tab)
     .setColorForeground(azulescuro)
     .setBroadcast(false)
     .setDecimalPrecision(0)
     .setPosition(posX, posY+240)
     .setSize(580, 52)
     .setHandleSize(18)
     .setRange(rangeMin, rangeMax)
     .setRangeValues(rangeMin, rangeMax)
     .setLabel("Evento 4")
     .setFont(font)
     .setId(400 + ID)
     .setLabelVisible(false)
     //.setBroadcast(true)
     .lock()
     .moveTo(tab)
     ;

  cp5.addTextlabel("nomeEvento_4" + "_" + tab)
     .setText("EVENTO 4")
     .setPosition( (posX + 230) , (posY + 248) )
     .setColorValue(255)
     .setFont(font)
     .moveTo(tab)
     ;

  nomeNumbox = "numMiniEvento_4" + "_" + tab;
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(100, 20)
     .setPosition( (posX + 5) , (posY + 255) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     .moveTo(tab)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numMaxiEvento_4" + "_" + tab;
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 495) , (posY + 255) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     .moveTo(tab)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numIntvEvento_4" + "_" + tab;
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 260) , (posY + 272) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font_min)
     .moveTo(tab)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );


//// - Range Slider EVENTO 5 - ////
  cp5.addRange("evento_5" + "_" + tab)
     .setColorForeground(azulescuro)
     .setBroadcast(false)
     .setDecimalPrecision(0)
     .setPosition(posX, posY+320)
     .setSize(580, 52)
     .setHandleSize(18)
     .setRange(rangeMin, rangeMax)
     .setRangeValues(rangeMin, rangeMax)
     .setLabel("Evento 5")
     .setFont(font)
     .setId(500 + ID)
     .setLabelVisible(false)
     //.setBroadcast(true)
     .lock()
     .moveTo(tab)
     ;

  cp5.addTextlabel("nomeEvento_5" + "_" + tab)
     .setText("EVENTO 5")
     .setPosition( (posX + 230) , (posY + 328) )
     .setColorValue(255)
     .setFont(font)
     .moveTo(tab)
     ;

  nomeNumbox = "numMiniEvento_5" + "_" + tab;
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(100, 20)
     .setPosition( (posX + 5) , (posY + 335) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     .moveTo(tab)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numMaxiEvento_5" + "_" + tab;
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 495) , (posY + 335) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font)
     .moveTo(tab)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
  
  nomeNumbox = "numIntvEvento_5" + "_" + tab;
  cp5.addNumberbox(nomeNumbox)
     .setLabel(" ")
     .setSize(80, 20)
     .setPosition( (posX + 260) , (posY + 350) )
     .setDecimalPrecision(1)
     .setValue(0)
     .setMin(0)
     .setMax(18000)
     .setDirection(Controller.HORIZONTAL)
     .setFont(font_min)
     .moveTo(tab)
     ;                 
  makeEditable( cp5.get(Numberbox.class, nomeNumbox) );
}


//===========================================================================================
//  código para numberbox editável - funções que mapeiam numberbox para ser editado com
//                                    o teclado
//===========================================================================================
public void makeEditable( Numberbox n )
{
  final NumberboxInput nin = new NumberboxInput( n ); // custom input handler for the numberbox
  n.onClick(new CallbackListener()
  {
    public void controlEvent(CallbackEvent theEvent)
    {
      nin.setActive( true ); 
    }
  }
  ).onLeave(new CallbackListener() {
    public void controlEvent(CallbackEvent theEvent)
    {
      nin.setActive( false ); nin.submit();
    }
  });
}



// input handler for a Numberbox that allows the user to 
// key in numbers with the keyboard to change the value of the numberbox

public class NumberboxInput
{
  String text = "";

  Numberbox n;

  boolean active;

  
  NumberboxInput(Numberbox theNumberbox)
  {
    n = theNumberbox;
    registerMethod("keyEvent", this ); 
  }

  public void keyEvent(KeyEvent k) {
    // only process key event if input is active 
    if (k.getAction()==KeyEvent.PRESS && active)
    {
      if (k.getKey()=='\n')
      { // confirm input with enter
        submit();
        //println("class NumberboxInput: Valor digitado de " + n.getValue() + 
        //        " no objeto = " + n.getName());
        //sentinela.dedurarNumberbox( n.getName(), n.getValue() );
        //setarVetorSaida(true);
        //setarValoresNumberbox(true);
        return;
      }
      else if(k.getKeyCode()==BACKSPACE)
      { 
        text = text.isEmpty() ? "":text.substring(0, text.length()-1);
        //text = ""; // clear all text with backspace
      }
      else if(k.getKey()<255)
      {
        // check if the input is a valid (decimal) number
        final String regex = "\\d+([.]\\d{0,2})?";
        String s = text + k.getKey();
        if ( java.util.regex.Pattern.matches(regex, s ) )
        {
          text += k.getKey();
        }
      }
      n.getValueLabel().setText(this.text);
    }
  }

  public void setActive(boolean b)
  {
    active = b;
    if(active)
    {
      n.getValueLabel().setText("");
      text = ""; 
    }
  }
  
  public void submit()
  {
    if (!text.isEmpty())
    {
      n.setValue( PApplet.parseFloat( text ) );
      text = "";
      //println("Interface:  Valor digitado de " + n.getValue() + " em " + n.getName()); println();
      sentinela.dedurarNumberbox( n.getName(), n.getValue() );
      setarVetorSaida(true);
      setarValoresNumberbox(true);
    } 
    else
    {
      n.getValueLabel().setText("" + n.getValue());
    }
  }
}


//===========================================================================================
//  setarVetorSaida() - funçãos que seta valor de um tempo em Sentinela no vetor de saída
//
//===========================================================================================
public void setarVetorSaida(boolean mouse)
{
  int evento = sentinela.dedoDuro_evento();
  int porta = sentinela.dedoDuro_porta();
  
  String tipo = sentinela.dedoDuro_tipoObjeto();
  
  boolean tempoInicialModificado = sentinela.dedoDuro_mudouTempoInicial();
  boolean tempoFinalModificado = sentinela.dedoDuro_mudouTempoFinal();
  boolean tempoIntervaloModificado = sentinela.dedoDuro_mudouIntervalo();
  
  float tempoInicial = sentinela.dedoDuro_tempoInicial();
  float tempoFinal = sentinela.dedoDuro_tempoFinal();
  float tempoIntervalo = sentinela.dedoDuro_tempoIntervalo();
  
  if (mouse == true)
  {
    if ( tipo == "Numberbox" )  //se os dados em Sentinela são de um Numberbox
    {
      if ( tempoInicialModificado == true )  //se o Numberbox em Sentinela é de tempo inicial
      {
        switch (evento)
        {
          case 1: evento1Min[porta - 1] = round(tempoInicial); break;
          case 2: evento2Min[porta - 1] = round(tempoInicial); break;
          case 3: evento3Min[porta - 1] = round(tempoInicial); break;
          case 4: evento4Min[porta - 1] = round(tempoInicial); break;
          case 5: evento5Min[porta - 1] = round(tempoInicial); break;
        }
      }
      else if ( tempoFinalModificado == true )  //se é de tempo final
      {
        switch (evento)
        {
          case 1: evento1Max[porta - 1] = round(tempoFinal); break;
          case 2: evento2Max[porta - 1] = round(tempoFinal); break;
          case 3: evento3Max[porta - 1] = round(tempoFinal); break;
          case 4: evento4Max[porta - 1] = round(tempoFinal); break;
          case 5: evento5Max[porta - 1] = round(tempoFinal); break;
        }
      }
      else if ( tempoIntervaloModificado == true )  //se é de intervalo de tempo
      {
        switch (evento)
        {
          case 1: evento1Max[porta - 1] = round(tempoIntervalo); break;
          case 2: evento2Max[porta - 1] = round(tempoIntervalo); break;
          case 3: evento3Max[porta - 1] = round(tempoIntervalo); break;
          case 4: evento4Max[porta - 1] = round(tempoIntervalo); break;
          case 5: evento5Max[porta - 1] = round(tempoIntervalo); break;
        }
      }
    }
    //println("setarVetorSaida(): CHAMANDO debug() DE SENTINELA!");
    //sentinela.debug("setarVetorSaida() em PROGRAMA");
  }
}
//fim da função setarVetorSaida()


//===========================================================================================
//  setarValoresNumberbox() - funçãos que seta Valores dos Numberbox de um mesmo Evento
//
//        Se tempoInicial modificado:    tempoFinal = tempoInicial + tempoIntervalo
//        Se tempoIntervalo modificado:  tempoFinal = tempoInicial + tempoIntervalo
//        Se tempoFinal modificado:      tempoIntervalo = tempoInicial + tempoFinal
//===========================================================================================
public void setarValoresNumberbox(boolean mouse)
{
  int evento = sentinela.dedoDuro_evento();
  int porta = sentinela.dedoDuro_porta();
  
  boolean tempoInicialModificado = sentinela.dedoDuro_mudouTempoInicial();
  boolean tempoFinalModificado = sentinela.dedoDuro_mudouTempoFinal();
  boolean tempoIntervaloModificado = sentinela.dedoDuro_mudouIntervalo();
  
  float tempoInicial = sentinela.dedoDuro_tempoInicial();
  float tempoFinal = sentinela.dedoDuro_tempoFinal();
  float tempoIntervalo = sentinela.dedoDuro_tempoIntervalo();
  
  String numberboxTempoInicial = sentinela.dedoDuro_numberboxTempoInicial();
  String numberboxTempoFinal = sentinela.dedoDuro_numberboxTempoFinal();
  String numberboxIntervaloTempo = sentinela.dedoDuro_numberboxIntervalo();
  String objetoSync = sentinela.dedoDuro_sync();
  String numberboxTempoInicialSync = sentinela.dedoDuro_TempoInicialSync();
  String numberboxTempoFinalSync = sentinela.dedoDuro_TempoFinalSync();
  String numberboxIntervaloSync = sentinela.dedoDuro_IntervaloSync();
  
  
  if (mouse == true)
  {
    if ( tempoInicialModificado == true ) //se Numberbox de tempo inicial foi utilizado
    {
      switch (evento)  //recuperar tempo em Numberbox de Fim para cálculo
      {
        case 1: tempoFinal = evento1Max[porta - 1]; break;
        case 2: tempoFinal = evento2Max[porta - 1]; break;
        case 3: tempoFinal = evento3Max[porta - 1]; break;
        case 4: tempoFinal = evento4Max[porta - 1]; break;
        case 5: tempoFinal = evento5Max[porta - 1]; break;
      }
      switch (evento)  //recuperar tempo em Numberbox de Intervalo para cálculo
      {
        case 1: tempoIntervalo = evento1Max[porta - 1] - tempoInicial; break;
        case 2: tempoIntervalo = evento2Max[porta - 1] - tempoInicial; break;
        case 3: tempoIntervalo = evento3Max[porta - 1] - tempoInicial; break;
        case 4: tempoIntervalo = evento4Max[porta - 1] - tempoInicial; break;
        case 5: tempoIntervalo = evento5Max[porta - 1] - tempoInicial; break;
      }
      if (tempoInicial > tempoFinal)
      {
        tempoFinal = tempoInicial;
        tempoIntervalo = tempoFinal - tempoInicial;
      }
      //setando valor dos Numberbox's do Evento e do Numberbox's em sync
      cp5.get(Numberbox.class, numberboxTempoInicialSync).setValue(tempoInicial);
      cp5.get(Numberbox.class, numberboxTempoFinal).setValue(tempoFinal);
      cp5.get(Numberbox.class, numberboxIntervaloTempo).setValue(tempoIntervalo);
      cp5.get(Numberbox.class, numberboxTempoFinalSync).setValue(tempoFinal);
      cp5.get(Numberbox.class, numberboxIntervaloSync).setValue(tempoIntervalo);
      if (evento == 1)
      {
        //setando valor dos Numberbox's do Evento e do Numberbox's em sync
        cp5.get(Numberbox.class, numberboxTempoInicialSync).setValue(tempoInicial);
        cp5.get(Numberbox.class, numberboxTempoFinal).setValue(tempoFinal);
        cp5.get(Numberbox.class, numberboxIntervaloTempo).setValue(tempoIntervalo);
        cp5.get(Numberbox.class, numberboxTempoFinalSync).setValue(tempoFinal);
        cp5.get(Numberbox.class, numberboxIntervaloSync).setValue(tempoIntervalo);
      }
      
      switch (evento)  //armazenar valor de tempo final no vetor de saída
      {
        case 1: evento1Max[porta - 1] = tempoFinal; break;
        case 2: evento2Max[porta - 1] = tempoFinal; break;
        case 3: evento3Max[porta - 1] = tempoFinal; break;
        case 4: evento4Max[porta - 1] = tempoFinal; break;
        case 5: evento5Max[porta - 1] = tempoFinal; break;
      }
    }
    else if ( tempoFinalModificado == true) //se Numberbox de tempo final foi utilizado
    {
      switch (evento)  //recuperar tempo em Numberbox de Início para cálculo
      {
        case 1: tempoInicial = evento1Min[porta - 1]; break;
        case 2: tempoInicial = evento2Min[porta - 1]; break;
        case 3: tempoInicial = evento3Min[porta - 1]; break;
        case 4: tempoInicial = evento4Min[porta - 1]; break;
        case 5: tempoInicial = evento5Min[porta - 1]; break;
      }
      if (tempoFinal < tempoInicial )
      {
        tempoFinal = tempoInicial;
        tempoIntervalo = tempoFinal - tempoInicial;
      }
      else
      {
        tempoIntervalo = tempoFinal - tempoInicial;
      }
      //setando valor dos Numberbox's do Evento e do Numberbox's em sync
      cp5.get(Numberbox.class, numberboxTempoInicialSync).setValue(tempoInicial);
      cp5.get(Numberbox.class, numberboxTempoFinal).setValue(tempoFinal);
      cp5.get(Numberbox.class, numberboxIntervaloTempo).setValue(tempoIntervalo);
      cp5.get(Numberbox.class, numberboxTempoFinalSync).setValue(tempoFinal);
      cp5.get(Numberbox.class, numberboxIntervaloSync).setValue(tempoIntervalo);
      switch (evento)  //armazenar valor de tempo final no vetor de saída
      {
        case 1: evento1Max[porta - 1] = tempoFinal; break;
        case 2: evento2Max[porta - 1] = tempoFinal; break;
        case 3: evento3Max[porta - 1] = tempoFinal; break;
        case 4: evento4Max[porta - 1] = tempoFinal; break;
        case 5: evento5Max[porta - 1] = tempoFinal; break;
      }
    }
    else if ( tempoIntervaloModificado == true) //se Numberbox de intervalo de tempo foi utilizado
    {
      switch (evento)  //recuperar tempo em Numberbox de Início para cálculo
      {
        case 1: tempoInicial = evento1Min[porta - 1]; break;
        case 2: tempoInicial = evento2Min[porta - 1]; break;
        case 3: tempoInicial = evento3Min[porta - 1]; break;
        case 4: tempoInicial = evento4Min[porta - 1]; break;
        case 5: tempoInicial = evento5Min[porta - 1]; break;
      }
      switch (evento)  //recuperar tempo em Numberbox de Fim para cálculo
      {
        case 1: tempoFinal = evento1Max[porta - 1]; break;
        case 2: tempoFinal = evento2Max[porta - 1]; break;
        case 3: tempoFinal = evento3Max[porta - 1]; break;
        case 4: tempoFinal = evento4Max[porta - 1]; break;
        case 5: tempoFinal = evento5Max[porta - 1]; break;
      }
      if (tempoIntervalo == 0) { tempoFinal = tempoInicial; }
      else { tempoFinal = tempoInicial + tempoIntervalo; }
      switch (evento)  //armazenar valor de tempo final no vetor de saída
      {
        case 1: evento1Max[porta - 1] = tempoFinal; break;
        case 2: evento2Max[porta - 1] = tempoFinal; break;
        case 3: evento3Max[porta - 1] = tempoFinal; break;
        case 4: evento4Max[porta - 1] = tempoFinal; break;
        case 5: evento5Max[porta - 1] = tempoFinal; break;
      }
      //setando valor dos Numberbox's do Evento e do Numberbox's em sync
      cp5.get(Numberbox.class, numberboxTempoInicialSync).setValue(tempoInicial);
      cp5.get(Numberbox.class, numberboxTempoFinalSync).setValue(tempoFinal);
      cp5.get(Numberbox.class, numberboxTempoInicial).setValue(tempoInicial);
      cp5.get(Numberbox.class, numberboxTempoFinal).setValue(tempoFinal);
      cp5.get(Numberbox.class, numberboxIntervaloSync).setValue(tempoIntervalo);
    }
  }
}
//fim da função setarValoresNumberbox()


//===========================================================================================
//  setLock() - funçãos que ativa/desativa elemento de interface
//
//===========================================================================================
public void setLock(Controller theController, boolean theValue)
{
  theController.setLock(theValue);
  
  if(theValue)
  {
    theController.setColorBackground(color(100,100));
  }
  else
  {
    theController.setColorBackground(color(col));
  }
}


//===========================================================================================
//  estadoBotaoArmar() - funçãos que ativa/desativa botão ARMAR na interface
//
//            true  -> Ativo
//            false -> Desativado
//
//===========================================================================================
public void estadoBotaoArmar(boolean estaAtivo)
{
  if(estaAtivo)
  {
    setLock(cp5.getController("armar_default"), false);
  }
  else if(!estaAtivo)
  {
    setLock(cp5.getController("armar_default"), true);
  }
}


//===========================================================================================
//  estadoBotaoIniciar() - funçãos que ativa/desativa botão INICIAR na interface
//
//            true  -> Ativo
//            false -> Desativado
//
//===========================================================================================
public void estadoBotaoIniciar(boolean estaAtivo)
{
  if(estaAtivo)
  {
    setLock(cp5.getController("iniciar_default"), false);
    armado = true;
  }
  else if(!estaAtivo)
  {
    setLock(cp5.getController("iniciar_default"), true);
    armado = false;
  }
}


//===========================================================================================
/*                  --------------  Fim do Programa  --------------                        */
//===========================================================================================
//===========================================================================================
//  Sentinela - Classe que armazena e retorna valores do ID, dos tempos e o nome do último 
//              "range slider" ou "knob de tempo máximo" modificado.
//
//    objetivo: implementação de um ajuste fino para precisão de 1 ms via interface
//
//===========================================================================================


class Sentinela
{
  //Declaração dos campos da classe Sentinela
  boolean modificouPorta = false;  //se Porta (Tab) sofreu modificação
  String nomeObjeto = "";    //armazena nome do último elemento de interface modificado
  int portaObjeto = 0;    //armazena Porta do último elemento de interface modificado
  int evento = 0;    //armazena o Evento modificado se existir
  
  boolean modificouObjeto = false;  //se nomeObjeto sofreu modificação
  String tipoObjeto = ""; //armazena o tipo do objeto armazenado. se Numberbox ou Slider por ex.
  int objetoID = 0;    //armazena o último elemento de interface modificado
  
  boolean modificouTempoInicial = false; //se tempoInicial foi alteração
  float memoriaTempoInicial = -1;    //variavel para armazenar último valor modificado
                                    // do tempo inicial de qualquer range slider de 
                                    // eventos da interface
  
  boolean modificouTempoFinal = false; //se tempoFinal sofreu alteração
  float memoriaTempoFinal = -1;    //variavel para armazenar último valor modificado
                                  // do tempo final de qualquer range slider de
                                  // eventos da interface ou do knon TEMPO MÁXIMO

  boolean modificouTempoIntervalo = false;    //se tempo do Intervalo dofreu alteração
  float memoriaTempoIntervalo = -1;    //variavel para armazenar último valor modificado
                                      // do tempo de um Numberbox de Intervalo

  String objeto = "";    //recebe o nome do objeto modificado (rangeSlider ou knob)
  String objetoSync = ""; // recebe objeto em sync se houver (Evento 1).

  String[] tab = {"_Porta1", "_Porta2" , "_Porta3" , "_Porta4" ,
                  "_Porta5", "_Porta6" , "_Porta7" , "_Porta8" ,
                  "_Porta9", "_Porta10", "_Porta11", "_Porta12"};
  
  //armazena inicio dos nomes dos Numberbox de Intervalo de tempo
  String[] nomeNumboxIntervalo = {"numIntvE1"     , "numIntvEvento_1", "numIntvEvento_2",
                                  "numIntvEvento_3", "numIntvEvento_4", "numIntvEvento_5"};

  //armazena inicio dos nomes dos Numberbox de tempo de Início
  String[] nomeNumboxMinimo = {"numMiniE1"     , "numMiniEvento_1", "numMiniEvento_2",
                               "numMiniEvento_3", "numMiniEvento_4", "numMiniEvento_5"};

  //armazena inicio dos nomes dos Numberbox de tempo final
  String[] nomeNumboxMaximo = {"numMaxiE1"     , "numMaxiEvento_1", "numMaxiEvento_2",
                               "numMaxiEvento_3", "numMaxiEvento_4", "numMaxiEvento_5"};
 
 //as variáveis de Numberbox de tempo estão relacionadas ao último objeto observado por Sentinela
 String numberboxTempoInicial = "";    //armazena nome do Numberbox referente ao tempo inicial
 String numberboxTempoFinal = "";    //armazena nome do Numberbox referente ao tempo final
 String numberboxIntervalo = "";    //armazena nome do Numberbox referente ao intervalo de tempo
 String numberboxTempoInicialSync = "";    //Numberbox relacionado ao de tempo inicial - Sync
 String numberboxTempoFinalSync = "";    //Numberbox relacionado ao de tempo final - Sync
 String numberboxIntervaloSync = "";    //Numberbox relacionado ao de intervalo de tempo - Sync


 //Criação do construtor da classe Sentinela
 Sentinela(int ID, float tempoInicial, float tempoFinal)
 {
   objetoID = ID;
   memoriaTempoInicial = tempoInicial;
   memoriaTempoFinal = tempoFinal;
 }

 //////////////////////////////
 //Métodos da classe Sentinela

 //Função que recebe informações para montagem da base de dados do último controlador observado
 public void dedurar(String nome, float tempoInicial, float tempoFinal, boolean mousePressed)
 {
    if (mousePressed == true)
    {
     float memoria = 0;  //variável para verificar se houve modificação de tempo em algum campo
     String memoNome = ""; //variável para verificar se nome foi modificado

     //seta tipo do objeto
     memoNome = nome.substring(0, 4);
     if ( nome.equals("num") == true ) { tipoObjeto = "Numberbox"; }
     else if ( nome.equals("sld") == true ) { tipoObjeto = "Slider"; }
     else { tipoObjeto = ""; }
     
     // modifica o nome do objeto e assinala se houve alteração de valor
     memoNome = nomeObjeto;
     nomeObjeto = nome;
     if (memoNome.equals(nomeObjeto) ){ modificouObjeto = false; }
     else { modificouObjeto = true; }
     
     // modifica a Porta do objeto e assinala se houve alteração de valor
     memoria = portaObjeto;
     setaPortaObjeto();
     if (memoria != portaObjeto){ modificouPorta = true; }
     else { modificouPorta = false; }
     
     // modifica e tempoInicial e assinala se houve alteração de valor
     setaTempoInicial(tempoInicial);
     
     // modifica e tempoFinal e assinala se houve alteração de valor
     setaTempoFinal(tempoFinal);
     
     //seta variavel memoriaTempoIntervalo
     float tempo = memoriaTempoFinal - memoriaTempoInicial;
     setaTempoIntervalo(tempo);
     
     // seta evento
     setaEvento();
     
     //seta nomes de objetos Numberbox se existir
     setaNomesNumberbox();
     
     //função de mensagens para debug no console
     debug("dedurar()");
    }
 }
 
 //Função que recebe informações para montagem da base de dados do último Numberbox observado
 public void dedurarNumberbox(String nome, float tempo)
 {
   nomeObjeto = nome;
   tipoObjeto = "Numberbox";
   
   //seta Porta atrelada ao Numberbox dedurado
   setaPortaObjeto();
   
   //seta Evento atrelado ao Numberbox dedurado
   setaEvento();
   
   //setando tempo final e/ou inicial do evento
   char tipoNumberbox = nomeObjeto.charAt(4);
   
   switch (tipoNumberbox)
   {
     case 'i':    //quando tempo inicial
       setaTempoInicial(tempo);
       
       break;
     
     case 'a':    //quando tempo final
       setaTempoFinal(tempo);
       break;
     
     case 'n':    //quando tempo do intervalo foi modificado
       setaTempoIntervalo(tempo);
       break;
     
     default:    //se o tipo não puder ser indentificado
       //println("dedurarNumerbox(): Tipo de Numberbox não identificado! ");
       //println("dedurarNumerbox(): " + tipoNumberbox );
   }
   
   
   String trecho;
   trecho = nome.substring(7, 9);
   if ( trecho.equals("E1") == true )
   {
     //println("dedurarNumberbox(): Porta do Numberbox dedurado a sentinela é: " + portaObjeto);
   }
   else
   {
     trecho = nome.substring(7, 15);
     //println("dedurarNumberbox(): Aba dedurada a sentinela: " + trecho);
   }
   
   //seta nomes de objetos Numberbox se existir
   setaNomesNumberbox();
   
   //função de mensagens para debug no console
   //debug("dedurarNumberbox()");
 }
 //fim da função dedurarNumberbox()

 
 public String nomeNumboxIntervalo(int i)  //retorna dados do vetor nomeNumboxIntervalo[]
 { return nomeNumboxIntervalo[i]; }
 
 
 public String nomeNumboxMinimo(int i)  //retorna dados do vetor nomeNumboxIntervalo[]
 { return nomeNumboxMinimo[i]; }
 
 
 public String nomeNumboxMaximo(int i)  //retorna dados do vetor nomeNumboxIntervalo[]
 { return nomeNumboxMaximo[i]; }


 public String dedoDuro_nome()  //retorna String do nome do elemento de inteface modificado
 { return objeto; }
 
 
 public String dedoDuro_tipoObjeto()  //retorna tipo do objeto, se Numberbox ou se Slider por exemplo
 { return tipoObjeto; }

 
 public String dedoDuro_sync()  //retorna String no nome do elemento em sync (evento 1)
 { return objetoSync; }
 
 
 public String dedoDuro_TempoInicialSync()  //retorna String no nome do elemento em sync (evento 1)
 { return numberboxTempoInicialSync; }
 
 
 public String dedoDuro_TempoFinalSync()  //retorna String no nome do elemento em sync (evento 1)
 { return numberboxTempoFinalSync; }
 
 
 public String dedoDuro_IntervaloSync()  //retorna String no nome do elemento em sync (evento 1)
 { return numberboxIntervaloSync; }


 public int dedoDuro_ID()  //retorna ID do objeto armazenado
 { return objetoID; }


 public int dedoDuro_porta()  //retorna numero da porta do objeto armazenado
 { return portaObjeto; }


 public String dedoDuro_tab()  //retorna página (porta) do objeto armazenado
 { return tab[portaObjeto - 1]; }


 public int dedoDuro_evento()  //retorna evento do objeto armazenado (!= 0 se existir)
 { return evento; }


 public float dedoDuro_tempoInicial()  //retorna float com tempo Inicial do elemento armazenado
 { return memoriaTempoInicial; }


 public boolean dedoDuro_mudouTempoInicial() //retorna se dado de tempo Inicial foi modificado
 { return modificouTempoInicial; }


 public float dedoDuro_tempoFinal()  //retorna float com tempo Final do elemento armazenado
 { return memoriaTempoFinal; }


 public boolean dedoDuro_mudouTempoFinal() //retorna se dado de tempo Final foi modificado
 { return modificouTempoFinal; }  


 public float dedoDuro_tempoIntervalo()  //retorna float com tempo Final do elemento armazenado
 { return memoriaTempoIntervalo; } 

 
 public boolean dedoDuro_mudouIntervalo()  //retorna se dado de intervalo de tempo foi modificado
 { return modificouTempoIntervalo; }
 
 
 public String dedoDuro_numberboxTempoInicial() //retorna nome do Numberbox de tempo inicial relacionado
 { return numberboxTempoInicial; }
 
 
 public String dedoDuro_numberboxTempoFinal() //retorna nome do Numberbox de tempo final relacionado
 { return numberboxTempoFinal; }
 
 
 public String dedoDuro_numberboxIntervalo()//retorna nome do Numberbox de intervalo de tempo relacionado
 { return numberboxIntervalo; }
 

  //////////////////////////////////////
 // métodos para setar campos da classe 

 private void setaEvento()    // seta nome do objeto 
 {
   char tipo = tipoObjeto.charAt(0);
   char eventoNome = '0';
  
   switch (tipo)
   {
     case 'N':
       if ( nomeObjeto.charAt(8) == '1' )
       {
         eventoNome = nomeObjeto.charAt(8);
         evento = PApplet.parseInt(eventoNome) - 48;
       }
       else if ( nomeObjeto.charAt(8) == 'v' )
       {
         eventoNome = nomeObjeto.charAt(14);
         evento = PApplet.parseInt(eventoNome) - 48;
       }
       else
       {
         println("Sentinela: setaEvento(): ERRO ao reconhecer nome do objeto");
         println("Sentinela: setaEvento(): String nomeObjeto = " + nomeObjeto);
         println("Sentinela: setaEvento(): char eventoNome = " + eventoNome);
       }
       break;
     case 'S':
       eventoNome = nomeObjeto.charAt(8);
       evento = PApplet.parseInt(eventoNome) - 48;
       break;
     default:
       println("setaEvento() em Sentinela: Falha ao tentar setar tempo inicial em sentinela");
       println("setaEvento() em Sentinela: O nome de objeto inválido é " + nomeObjeto);
   } 
 }


 private void setaPortaObjeto()    // seta Porta do objeto
 { 
   int i = nomeObjeto.length();
   char porta = nomeObjeto.charAt(i - 1);
   portaObjeto = PApplet.parseInt(porta) - 48;
 }


 //seta tempo inicial e assinala se houve modificação
 private void setaTempoInicial(float tempo)
 {
   char tipo = tipoObjeto.charAt(0);
   float memoria = -1;  //variável para verificar se houve modificação de tempo
   
   resetModificouTempo();
   memoriaTempoFinal = -1;
   memoriaTempoIntervalo = -1;
   
   switch (tipo)
   {
     case 'N':
       memoriaTempoInicial = tempo;
       if (memoria != memoriaTempoInicial) { modificouTempoInicial = true; }
       else { modificouTempoInicial = false; }
       break;
     
     case 'S':
       memoriaTempoInicial = tempo;
       if (memoria != memoriaTempoInicial) { modificouTempoInicial = true; }
       else { modificouTempoInicial = false; }
       break;
     
     default:
       println("setaTempoInicial() em Sentinela: Falha ao tentar setar tempo inicial de "
               + tempo + "ms em sentinela");
       println("setaTempoInicial() em Sentinela: O nome de objeto inválido é " + nomeObjeto);
   }
 }


 //seta tempo final e assinala se houve modificação
 private void setaTempoFinal(float tempo)
 {
   char tipo = tipoObjeto.charAt(0);
   float memoria = memoriaTempoFinal;  //variável para verificar se houve modificação de tempo
   
   resetModificouTempo();
   memoriaTempoInicial = -1;
   memoriaTempoIntervalo = -1;
   
   switch (tipo)
   {
     case 'N':
       memoriaTempoFinal = tempo;
       if (memoria != memoriaTempoFinal) { modificouTempoFinal = true; }
       else { modificouTempoFinal = false; }
       break;
     
     case 'S':
       memoriaTempoFinal = tempo;
       if (memoria != memoriaTempoFinal) { modificouTempoFinal = true; }
       else { modificouTempoFinal = false; }
       break;
     
     default:
       println("setaTempoFinal() em Sentinela: Falha ao tentar setar tempo final de "
               + tempo + "ms em sentinela");
       println("setaTempoFinal() em Sentinela: O nome de objeto inválido é " + nomeObjeto);
   }
 }
 
 //seta tempo do intervalo e assinala se houve modificação
 private void setaTempoIntervalo(float tempo)
 {
   char tipo = tipoObjeto.charAt(0);
   float memoria = memoriaTempoIntervalo;//variável para verificar se houve modificação de tempo
   
   resetModificouTempo();
   memoriaTempoInicial = -1;
   memoriaTempoFinal = -1;
   
   switch (tipo)
   {
     case 'N':
       memoriaTempoIntervalo = tempo;
       if (memoria != memoriaTempoIntervalo) { modificouTempoIntervalo = true; }
       else { modificouTempoIntervalo = false; }
       break;
     
     case 'S':
       memoriaTempoIntervalo = tempo;
       if (memoria != memoriaTempoIntervalo) { modificouTempoIntervalo = true; }
       else { modificouTempoIntervalo = false; }
       break;
     
     default:
       println("setaTempoIntervalo() em Sentinela: Falha ao tentar setar tempo final de "
               + tempo + "ms em sentinela");
       println("setaTempoIntervalo() em Sentinela: O nome de objeto inválido é " + nomeObjeto);
   }
 }


 // reseta verificadores de alteração de parâmetros de tempo
 private void resetModificouTempo()
 {
   modificouTempoInicial = false;
   modificouTempoFinal = false;
   modificouTempoIntervalo = false;
 }


 //função que seta variáveis com os nomes dos Numberbox relacionados por Evento
 private void setaNomesNumberbox()
 {
   numberboxTempoInicial = "";
   numberboxTempoFinal = "";
   numberboxIntervalo = "";
   numberboxTempoInicialSync = "";
   numberboxTempoFinalSync = "";
   numberboxIntervaloSync = "";
   
   
   char tipo = tipoObjeto.charAt(0);
   
   if ( tipo == 'N' )  //se for Numberbox
   {
     if ( modificouTempoInicial == true )  //se numberbox que entrou é de tempo inicial
     {
       numberboxTempoInicial = nomeObjeto;
       
       String trecho;
       trecho = nomeObjeto.substring(7, 9);
       if ( trecho.equals("E1") == true )
       {
         objetoSync = nomeNumboxMinimo[1] + tab[portaObjeto - 1];
         numberboxTempoInicialSync = objetoSync;
         
         numberboxTempoFinal = nomeNumboxMaximo[0] + tab[portaObjeto - 1];
         numberboxTempoFinalSync = nomeNumboxMaximo[1] + tab[portaObjeto - 1];
         
         numberboxIntervalo = nomeNumboxIntervalo[0] + tab[portaObjeto - 1];
         numberboxIntervaloSync = nomeNumboxIntervalo[1] + tab[portaObjeto - 1];
       }
       else if ( trecho.equals("Ev") )
       {
         if (evento == 1)
         {
           objetoSync = nomeNumboxMinimo[0] + tab[portaObjeto - 1];
           numberboxTempoInicialSync = objetoSync;
           
           numberboxTempoFinal = nomeNumboxMaximo[1] + tab[portaObjeto - 1];
           numberboxTempoFinalSync = nomeNumboxMaximo[0] + tab[portaObjeto - 1];
           
           numberboxIntervalo = nomeNumboxIntervalo[1] + tab[portaObjeto - 1];
           numberboxIntervaloSync = nomeNumboxIntervalo[0] + tab[portaObjeto - 1];
         }
         else { objetoSync = "noSync"; }
         numberboxTempoFinal = nomeNumboxMaximo[evento] + tab[portaObjeto - 1];
         numberboxIntervalo = nomeNumboxIntervalo[evento] + tab[portaObjeto - 1];
       }
       else
       {
         println("Sentinela: setaNomesNumberbox(): modificouTempoInicial == true");
         println("Sentinela: setaNomesNumberbox(): erro ao setar nomes de Numberbox");
       }    
     }
     
     else if ( modificouTempoFinal == true )  //se numberbox que entrou é de tempo final
     {
       numberboxTempoFinal = nomeObjeto;
       
       String trecho;
       trecho = nomeObjeto.substring(7, 9);
       if ( trecho.equals("E1") == true )
       {
         objetoSync = nomeNumboxMaximo[evento] + tab[portaObjeto - 1];
         numberboxTempoFinalSync = objetoSync;
         
         numberboxTempoInicial = nomeNumboxMinimo[0] + tab[portaObjeto - 1];
         numberboxTempoInicialSync = nomeNumboxMinimo[1] + tab[portaObjeto - 1];
         
         numberboxIntervalo = nomeNumboxIntervalo[0] + tab[portaObjeto - 1];
         numberboxIntervaloSync = nomeNumboxIntervalo[1] + tab[portaObjeto - 1];
       }
       else if ( trecho.equals("Ev") )
       {
         if (evento == 1)
           {
             objetoSync = nomeNumboxMaximo[0] + tab[portaObjeto - 1];
             numberboxTempoFinalSync = objetoSync;
             
             numberboxTempoInicial = nomeNumboxMinimo[1] + tab[portaObjeto - 1];
             numberboxTempoInicialSync = nomeNumboxMinimo[0] + tab[portaObjeto - 1];
             
             numberboxIntervalo = nomeNumboxIntervalo[1] + tab[portaObjeto - 1];
             numberboxIntervaloSync = nomeNumboxIntervalo[0] + tab[portaObjeto - 1];
           }
         else { objetoSync = "noSync"; }
         numberboxTempoInicial = nomeNumboxMinimo[evento] + tab[portaObjeto - 1];
         numberboxIntervalo = nomeNumboxIntervalo[evento] + tab[portaObjeto - 1];
       }
       else
       {
         println("Sentinela: setaNomesNumberbox(): modificouTempoFinal == true");
         println("Sentinela: setaNomesNumberbox(): erro ao setar nomes de Numberbox");
       }    
     }
     
     else if ( modificouTempoIntervalo == true )  //se numberbox que entrou é de intervalo de tempo
     {
       numberboxIntervalo = nomeObjeto;
       
       String trecho;
       trecho = nomeObjeto.substring(7, 9);
       if ( trecho.equals("E1") == true )
       {
         objetoSync = nomeNumboxIntervalo[1] + tab[portaObjeto - 1];
         numberboxIntervaloSync = objetoSync;
         
         numberboxTempoInicial = nomeNumboxMinimo[0] + tab[portaObjeto - 1];
         numberboxTempoInicialSync = nomeNumboxMinimo[1] + tab[portaObjeto - 1];
         
         numberboxTempoFinal = nomeNumboxMaximo[0] + tab[portaObjeto - 1];
         numberboxTempoFinalSync = nomeNumboxMaximo[1] + tab[portaObjeto - 1];
       }
       else if ( trecho.equals("Ev") )
       {
         if (evento == 1)
           {
             objetoSync = nomeNumboxIntervalo[0] + tab[portaObjeto - 1];
             numberboxIntervaloSync = objetoSync;
             
             numberboxTempoInicial = nomeNumboxMinimo[1] + tab[portaObjeto - 1];
             numberboxTempoInicialSync = nomeNumboxMinimo[0] + tab[portaObjeto - 1];
             
             numberboxTempoFinal = nomeNumboxMaximo[1] + tab[portaObjeto - 1];
             numberboxTempoFinalSync = nomeNumboxMaximo[0] + tab[portaObjeto - 1];
             
           }
         else { objetoSync = "noSync"; }
         numberboxTempoInicial = nomeNumboxMinimo[evento] + tab[portaObjeto - 1];
         numberboxTempoFinal = nomeNumboxMaximo[evento] + tab[portaObjeto - 1];
       }
       else
       {
         println("Sentinela: setaNomesNumberbox(): modificouTempoIntervalo == true");
         println("Sentinela: setaNomesNumberbox(): erro ao setar nomes de Numberbox");
       }    
     }
     
     else
     {
       println("Sentinela: setaNomesNumberbox(): erro ao setar nomes de Numberbox");
       println("Sentinela: setaNomesNumberbox(): nome do objeto de entrada inválido");
     }
   }
 }
 //fim da função setaNomesNumberbox()


 public void debug(String funcao)
 {
   println();
   println("-------   -------  -------  -------  -------");
   println("         INICIO debug() em Sentinela        ");
   println("           " + funcao);
   println("-------   -------  -------  -------  -------");
   println("O elemento de interface pertence ao Evento " + evento
                                                         + " na Porta " + portaObjeto);
   println("O label do elemento de interface = " + nomeObjeto);
   println("O tempo inicial foi modificado? " + modificouTempoInicial);
   println("O tempo final foi modificado? " + modificouTempoFinal);
   println("O tempo do intervalo foi modificado? " + modificouTempoIntervalo);
   println("tempoInicial guardado em sentinela: " + memoriaTempoInicial);
   println("tempoFinal guardado em sentinela: " + memoriaTempoFinal);
   println("tempoIntervalo guardado em sentinela: " + memoriaTempoIntervalo);
   println("Objeto em Sync: " + objetoSync);
   println("Numberbox tempo inicial: " + numberboxTempoInicial);
   println("Numberbox tempo inicial em Sync: " + numberboxTempoInicialSync);
   println("Numberbox tempo final: " + numberboxTempoFinal);
   println("Numberbox tempo final em Sync: " + numberboxTempoFinalSync);
   println("Numberbox intervalo de tempo: " + numberboxIntervalo);
   println("Numberbox intervalo de tempo em Sync: " + numberboxIntervaloSync);
   println("-------   -------  -------  -------  -------");
   println("          FIM debug() em Sentinela          ");
   println("           " + funcao);
   println("-------   -------  -------  -------  -------");
   println();
 }
}
  public void settings() {  size(1120, 700); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "interfacePC_Normae_v0_9_1" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

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
 void dedurar(String nome, float tempoInicial, float tempoFinal, boolean mousePressed)
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
 void dedurarNumberbox(String nome, float tempo)
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

 
 String nomeNumboxIntervalo(int i)  //retorna dados do vetor nomeNumboxIntervalo[]
 { return nomeNumboxIntervalo[i]; }
 
 
 String nomeNumboxMinimo(int i)  //retorna dados do vetor nomeNumboxIntervalo[]
 { return nomeNumboxMinimo[i]; }
 
 
 String nomeNumboxMaximo(int i)  //retorna dados do vetor nomeNumboxIntervalo[]
 { return nomeNumboxMaximo[i]; }


 String dedoDuro_nome()  //retorna String do nome do elemento de inteface modificado
 { return objeto; }
 
 
 String dedoDuro_tipoObjeto()  //retorna tipo do objeto, se Numberbox ou se Slider por exemplo
 { return tipoObjeto; }

 
 String dedoDuro_sync()  //retorna String no nome do elemento em sync (evento 1)
 { return objetoSync; }
 
 
 String dedoDuro_TempoInicialSync()  //retorna String no nome do elemento em sync (evento 1)
 { return numberboxTempoInicialSync; }
 
 
 String dedoDuro_TempoFinalSync()  //retorna String no nome do elemento em sync (evento 1)
 { return numberboxTempoFinalSync; }
 
 
 String dedoDuro_IntervaloSync()  //retorna String no nome do elemento em sync (evento 1)
 { return numberboxIntervaloSync; }


 int dedoDuro_ID()  //retorna ID do objeto armazenado
 { return objetoID; }


 int dedoDuro_porta()  //retorna numero da porta do objeto armazenado
 { return portaObjeto; }


 String dedoDuro_tab()  //retorna página (porta) do objeto armazenado
 { return tab[portaObjeto - 1]; }


 int dedoDuro_evento()  //retorna evento do objeto armazenado (!= 0 se existir)
 { return evento; }


 float dedoDuro_tempoInicial()  //retorna float com tempo Inicial do elemento armazenado
 { return memoriaTempoInicial; }


 boolean dedoDuro_mudouTempoInicial() //retorna se dado de tempo Inicial foi modificado
 { return modificouTempoInicial; }


 float dedoDuro_tempoFinal()  //retorna float com tempo Final do elemento armazenado
 { return memoriaTempoFinal; }


 boolean dedoDuro_mudouTempoFinal() //retorna se dado de tempo Final foi modificado
 { return modificouTempoFinal; }  


 float dedoDuro_tempoIntervalo()  //retorna float com tempo Final do elemento armazenado
 { return memoriaTempoIntervalo; } 

 
 boolean dedoDuro_mudouIntervalo()  //retorna se dado de intervalo de tempo foi modificado
 { return modificouTempoIntervalo; }
 
 
 String dedoDuro_numberboxTempoInicial() //retorna nome do Numberbox de tempo inicial relacionado
 { return numberboxTempoInicial; }
 
 
 String dedoDuro_numberboxTempoFinal() //retorna nome do Numberbox de tempo final relacionado
 { return numberboxTempoFinal; }
 
 
 String dedoDuro_numberboxIntervalo()//retorna nome do Numberbox de intervalo de tempo relacionado
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
         evento = int(eventoNome) - 48;
       }
       else if ( nomeObjeto.charAt(8) == 'v' )
       {
         eventoNome = nomeObjeto.charAt(14);
         evento = int(eventoNome) - 48;
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
       evento = int(eventoNome) - 48;
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
   portaObjeto = int(porta) - 48;
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


 void debug(String funcao)
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

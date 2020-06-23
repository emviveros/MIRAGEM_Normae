/********************************************************************
 * Biblioteca extraida de video aula Diego Pinto da D2G Tecnologia  *
 * Abstração de hardware para módulos relé.                         *
 *                                                Esteban Viveros   *
 ********************************************************************/

 #ifndef RELAYMODULE_HPP
 #define RELAYMODULE_HPP

//-------------------------------------------------------------------

#include "Arduino.h"

//-------------------------------------------------------------------

const size_t N_RELAYS = 12;  /* número de relés do módulo de reles */

//-------------------------------------------------------------------

/* Inicio da classe */
class RelayModule
{
  public:

    // Função construtora da classe. Requer os pinos do arduino e o 
    // estado de ativação do módulo de relés. false = LOW | true  = HIGH
    RelayModule(const int pin0, const int pin1,
                const int pin2, const int pin3,
                const int pin4, const int pin5,
                const int pin6, const int pin7,
                const int pin8, const int pin9,
                const int pin10, const int pin11,
                const bool activate_state);

    // Função set: seta o relé que vai ser acionado e o nível de ativação
    void set(const size_t index, const bool state);

    // Função on: liga o rele
    void on(const size_t index);

    // Função off: desliga o rele
    void off(const size_t index);

    // Função toggle: vira o estado do rele, inverte entre LOW e HIGH
    void toggle(const size_t index);

    void setALL(const bool state);  // seta todos os reles
    void onAll(void);               // liga todos os reles
    void offAll(void);              // desliga todos os reles
    void toggleAll(void);           // vira o estado de todos os reles

  private:

    bool a_state;         // armazena internamente o estado do módulo de rele

    int pins[N_RELAYS];   // armazena os pinos de cada rele
    bool state[N_RELAYS];         // armazena os estados de cada rele
  
};

//-------------------------------------------------------------------
 #endif // RELAYMODULE_HPP

/********************************************************************
 * Biblioteca extraida de video aula Diego Pinto da D2G Tecnologia  *
 * Abstração de hardware para módulos relé.                         *
 *                                                Esteban Viveros   *
 ********************************************************************/

#include "RelayModule.hpp"



// Função construtora da classe.
RelayModule::RelayModule(const int pin0, const int pin1,
                         const int pin2, const int pin3,
                         const int pin4, const int pin5,
                         const int pin6, const int pin7,
                         const int pin8, const int pin9,
                         const int pin10, const int pin11,
                         const bool activate_state):
  a_state(activate_state),
  pins{pin0, pin1, pin2, pin3, pin4, pin5,
       pin6, pin7, pin8, pin9, pin10, pin11},
  state{false, false, false, false, false, false,
        false, false, false, false, false, false}
{
  for (size_t i = 0; i < N_RELAYS; i++)
  {
    pinMode(pins[i], OUTPUT);           // Inicializa Pinos
    digitalWrite(pins[i], !a_state);    // Desliga reles na inicialização
  }
}

//-------------------------------------------------------------------

// Função set: seta o relé que vai ser acionado e o nível de ativação
void RelayModule::set(const size_t index, const bool state)
{
  bool s;

  s = state ^ (!a_state);

  this->state[index] = s;
  digitalWrite(pins[index], s);
}

//-------------------------------------------------------------------

// Função on: liga o rele
void RelayModule::on(const size_t index)
{
  state[index] = a_state;
  digitalWrite(pins[index], a_state);
}

//-------------------------------------------------------------------

// Função off: desliga o rele
void RelayModule::off(const size_t index)
{
  state[index] = a_state;
  digitalWrite(pins[index], !a_state);
}

//-------------------------------------------------------------------

// Função toggle: vira o estado do rele, inverte entre LOW e HIGH
void RelayModule::toggle(const size_t index)
{
  state[index] = !state[index];
  digitalWrite(pins[index], state[index]);
}

//-------------------------------------------------------------------    
    
void RelayModule::setALL(const bool state)  // seta todos os reles
{
  for (size_t i = 0; i < N_RELAYS; i++) set(i, state);
}
    
//-------------------------------------------------------------------    
    
void RelayModule::onAll(void)               // liga todos os reles
{
  for (size_t i = 0; i < N_RELAYS; i++) on(i);
}

//-------------------------------------------------------------------
    
void RelayModule::offAll(void)              // desliga todos os reles
{
  for (size_t i = 0; i < N_RELAYS; i++) off(i);
}
    
//-------------------------------------------------------------------
    
void RelayModule::toggleAll(void)   // vira o estado de todos os reles
{
  for (size_t i = 0; i < N_RELAYS; i++) toggle(i);
}

//-------------------------------------------------------------------

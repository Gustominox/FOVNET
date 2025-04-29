# NPR-2425

## WORKLOAD

- [x] Definir algoritmo de forwarding de V2Rsu
- [ ] Eventos do fog a implementar/loggar
- [x] Criar mapa de testes
- [x] Definir output para o output.csv
  
## Aplicações

### Carro

- [x] Enviar mensagem de awareness (Definir informação a se enviada)
- [x] Reagir mensagem a mensagem de awareness dos vizinhos
- [x] Implementar logica de mensagem V2Rsu
- [ ] Reagir a Mensagem vinda do Fog (*Evento*)
- [x] BroadCast de Beacon messages

### RSU

 - [x] Relay de Mensagens vindas da Rede veicular para o Fog (Unicast)
 - [ ] Enviar Mensagens para a Rede Veicular vindas do Fog (Brodcast)
 - [x] Beacon messages para informar a rede da sua existencia

### Fog
- [ ] Reagir a velocidade acima do limite


### Tasks a realizar 17/03-24/03

- [x] Mensagem de Awareness a chegar ao RSU (Carro) -> Juliana
- [x] Definir algoritmo e proto-implementação -> Gusto
- [ ] Fazer uma Mensagem F2V Chegar ao Rsu (por exemplo velocidade acima do limite) -> Carlos

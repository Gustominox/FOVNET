# NPR-2425

## WORKLOAD

- Definir algoritmo de forwarding de V2Rsu
- Eventos do fog a implementar/loggar
- Criar mapa de testes
- Definir output para o output.csv
  
## Aplicações

### Carro

- Enviar mensagem de awareness (Definir informação a se enviada)
- Reagir mensagem a mensagem de awareness dos vizinhos
- Implementar logica de mensagem V2Rsu
- Reagir a Mensagem vinda do Fog (*Evento*)
- BroadCast de Beacon messages

### RSU

 - Relay de Mensagens vindas da Rede veicular para o Fog (Unicast)
 - Enviar Mensagens para a Rede Veicular vindas do Fog (Brodcast)
 - Beacon messages para informar a rede da sua existencia

### Fog
- Reagir a velocidade acima do limite


### Tasks a realizar 17/03-24/03

- Mensagem de Awareness a chegar ao Fog (Carro) -> Juliana
- Definir algoritmo e proto-implementação -> Gusto
- Fazer uma Mensagem F2V Chegar ao Rsu (por exemplo velocidade acima do limite) -> Carlos
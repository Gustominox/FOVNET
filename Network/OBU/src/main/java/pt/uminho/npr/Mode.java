package pt.uminho.npr;

public enum Mode {
    DIRETO, // Direct mode
    SEARCH; // Search mode

    public String get() {
        switch (this) {
            case DIRETO:
                return "Modo direto: Envia a mensagem diretamente ao veículo";
            case SEARCH:
                return "Modo search: Inicia o processo de procura pelo veículo destino";
            default:
                throw new IllegalArgumentException("Modo desconhecido");
        }
    }
}
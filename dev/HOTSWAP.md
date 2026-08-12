# ATC Fabric 1.20.1 — desenvolvimento com HotSwap

Este modo executa o cliente de desenvolvimento com JetBrains Runtime 17, enhanced class redefinition/DCEVM e o transformer-agent do Architectury. Ele é separado da instância Prism usada para validar o JAR final.

## Arquivos instalados nesta máquina

- JBRSDK 17: `/mnt/dados/PrismLauncher-home/java/jbrsdk-17.0.14-hotswap`
- Agente: `.gradle/architectury/architectury-transformer-agent.jar`, preparado e adicionado automaticamente pelo Architectury Loom.

## Uso

No primeiro terminal:

```sh
./dev/run-fabric-hotswap.sh
```

Mantenha o Minecraft aberto. Depois de cada alteração Java, use outro terminal:

```sh
./dev/reload-fabric-hotswap.sh
```

O terminal do jogo deve mostrar estas duas etapas:

```text
[Architectury Transformer] Detected File Modification ...
[Architectury Transformer] Redefined N class(es) ...
```

Se não aparecer `Redefined`, não considere a alteração carregada.

## Limites importantes

- Alterações comuns no corpo de métodos e muitas mudanças de campos/métodos podem ser redefinidas sem reiniciar.
- Mudança de superclasse, remoção de interface, alterações precoces de bootstrap, metadados do mod e alguns Mixins ainda exigem reinício.
- Estado já criado não é automaticamente reconstruído. Reinicie quando o teste depender de registro de comando, inicialização estática, configuração carregada no startup ou entidade já existente.
- Para a assinatura de lançamento, sempre repita os testes prioritários com o JAR 2.1.0 limpo na instância Prism. HotSwap serve para o ciclo rápido de correção, não como evidência final do artefato empacotado.

É possível trocar os caminhos sem editar scripts:

```sh
ATC_HOTSWAP_JAVA_HOME=/caminho/do/jbr17 \
./dev/run-fabric-hotswap.sh
```

# Attract to Chat 2.1.0 — Auditoria de paridade 1.19.2

Data da validação: 21 de agosto de 2026.

## Resultado

A linha Minecraft 1.19.2 foi portada e validada localmente contra o source exato da release Minecraft 1.20.1 identificado pela tag `v2.1.0-r1`, commit `3bf5e1e`.

- Fabric 1.19.2: build, testes automatizados e smoke de servidor/cliente concluídos.
- Forge 1.19.2: build, testes automatizados e smoke de servidor/cliente concluídos.
- Fabric e Forge 1.20.1: build de regressão concluído no worktree limpo.
- Versão mantida em `2.1.0`.
- Schema mantido em `CONFIG_VERSION = 15`.
- Nenhuma integração Walkie-Chat foi incorporada.
- O cliente continua opcional; a lógica principal permanece server-side.
- O teste interativo Forge confirmou que texto ainda não enviado não atrai mobs e que cada envio gera somente um scan.

**Build + automated/runtime smoke validation passed; manual in-game behavior checklist remains.**

Os JARs estão tecnicamente prontos como candidatos de release. A publicação final ainda deve aguardar a execução da checklist de comportamento dentro do jogo, principalmente a conexão de clientes sem ATC e a movimentação real de cada família de mob.

## Baseline e método de comparação

A comparação não usou a linha futura 2.1.1 como referência. Foram comparados semanticamente os módulos `common`, `fabric` e `forge` da 1.19.2 com a tag lançada `v2.1.0-r1` da 1.20.1.

Foram revisados:

- inicialização e engine;
- árvore de comandos e autocomplete;
- config, presets e schema;
- `MoveToSoundGoal` e resolução de destinos;
- presença opcional do cliente e networking;
- traduções server-side;
- inicializadores Fabric e Forge;
- eventos de chat;
- mixins e descritores;
- arquivos de idioma;
- metadados e Gradle;
- testes existentes na release.

Diretórios gerados, IDE, `.gradle`, `build`, `run` e artefatos compilados foram ignorados durante a auditoria de source.

## Regressões encontradas e corrigidas

### Engine e mobs

- Restaurado o `GroundTargetResolver` robusto da release, adaptado às APIs 1.19.2.
- Corrigida a procura de chão que estava limitada a quatro blocos; a profundidade agora respeita o alcance aplicável.
- Restaurado o resultado detalhado de início de investigação por meio de `InvestigationStartResult`.
- Restaurados motivos de falha, contagem real de inícios e coordenadas formatadas no diagnóstico.
- Reaproximados `AtcEngine` e `MoveToSoundGoal` do comportamento lançado para mobs terrestres, aquáticos, voadores e de salto.
- Preservadas prioridade de combate, fuga de villagers, descarte de destinos inválidos e encerramento/cleanup de investigação.
- Preservada a resolução dinâmica de tipos de entidade registrados, incluindo entidades compatíveis de outros mods.

### Comandos, configuração e feedback

- Removido o comando regressivo `/atc config particles`.
- Mantidos somente os controles pessoais `/atc client particles enable|disable`.
- Impedido que uma preferência pessoal ative silenciosamente um controle global do servidor.
- Restaurado feedback idempotente para debug, CAPS, Vocal Fatigue, Anti-Spam e partículas pessoais.
- Preservadas as árvores de help, ignore individual/`@a`, Troll Mode, presets nativos/customizados, undo/reset, fatigue, anti-spam e debug.
- Confirmados `mod_version = 2.1.0`, schema 15 e partículas desativadas por padrão.

### Cliente opcional, chat e traduções

- Restaurados `ClientPresence`, inicializadores de cliente Fabric/Forge e o handshake opcional equivalente ao da release.
- Restaurado `ServerTranslations` com fallback server-side e suporte a `en_us`, `pt_br` e `es_es`.
- Alinhados os três arquivos de idioma com a baseline da release; todas as 116 chaves referenciadas pelo Java existem nos três idiomas.
- Removido o mixin Fabric redundante de chat.
- O Fabric usa o evento de mensagem apropriado e transforma `ChatMessageContent` em texto com `plain()`, evitando processamento duplicado.
- Corrigido o listener Forge que recebia tanto `ServerChatEvent.Preview` quanto `ServerChatEvent.Submitted`, atraindo mobs a cada alteração no campo de chat antes do Enter.
- O Forge agora escuta exclusivamente mensagens `Submitted`, respeita cancelamentos de outros mods e agenda a atração na thread principal do servidor.
- Confirmado em uma instância Prism real que digitar, usar Backspace e cancelar o chat produzem zero scans; um Enter produz exatamente um scan.
- O Forge mantém um único fluxo de processamento e não exige o mod no cliente.

### Build e apresentação

- Corrigido o erro de compilação Fabric causado pela diferença de tipo de `signedContent()` na 1.19.2.
- Adicionados testes JUnit reais ao módulo common.
- Corrigidos README e CHANGELOG para descrever partículas opt-in, clientes opcionais e os requisitos corretos de cada loader.
- Adicionado `.architectury-transformer/` ao ignore para não contaminar o source/release.

## Verificação de ausência da 2.1.1 e Walkie-Chat

Foi feita busca no código, recursos e conteúdo descompactado dos dois JARs por:

- `Walkie`;
- `walkie`;
- `BLOCK_RECEPTION`;
- `PROXIMITY_CHAT`;
- `WalkieChatCompat`.

Não houve ocorrência em código de produção, recursos, config, comandos, dependências ou JARs finais. A palavra aparece apenas nesta documentação/checklist como item explícito de exclusão.

## Diferenças intencionais de API

As diferenças abaixo são adaptações necessárias da 1.19.2, não diferenças de comportamento:

| Área | Minecraft 1.19.2 | Minecraft 1.20.1 |
|---|---|---|
| Registry de entidades | `Registry.ENTITY_TYPE` | `BuiltInRegistries.ENTITY_TYPE` |
| Acesso ao level | campo/API disponível na 1.19.2 | accessor `level()` |
| Criação de posição | `new BlockPos(Mth.floor(...))` | `BlockPos.containing(...)` |
| Feedback Brigadier | `sendSuccess(Component, boolean)` | `sendSuccess(Supplier<Component>, boolean)` |
| Conteúdo assinado Fabric | `ChatMessageContent#plain()` | `signedContent()` retorna texto diretamente |
| Loader/API | Forge 43.4.0 e Fabric API 0.76.0 | versões correspondentes da 1.20.1 |
| Metadados/resources | ranges e formatos da 1.19.2 | ranges e formatos da 1.20.1 |
| Nome do release Forge | `2.1.0` | release publicada como `2.1.0-r1` |

## Testes automatizados

Foram executados 13 testes na 1.19.2, com zero falhas e zero erros:

- árvore real de comandos;
- presença de `/atc client particles enable|disable`;
- inexistência de `/atc config particles`;
- schema 15 e defaults;
- aplicação de preset, undo e preservação de preferência pessoal;
- CAPS e exclamações;
- Vocal Fatigue;
- Anti-Spam;
- ignore individual e `@a`;
- Troll Mode;
- formatação de coordenadas/debug;
- `GroundTargetResolver` e destinos seguros.

A validação de regressão da 1.20.1 executou 7 testes, também com zero falhas e zero erros.

## Comandos de build executados

### Minecraft 1.19.2

```text
./gradlew clean build --no-daemon
```

Resultado: `BUILD SUCCESSFUL`, 30 tarefas, Fabric e Forge compilados e remapeados.

### Minecraft 1.20.1

```text
./gradlew clean build --no-daemon
```

Resultado: `BUILD SUCCESSFUL`, 27 tarefas. O worktree 1.20.1 permaneceu limpo.

O warning de source remap relacionado a `ForgeBiomeModifiers` é emitido pelo toolchain e não bloqueou compilação, testes nem os JARs runtime.

## Smoke tests de runtime

### Fabric 1.19.2 dedicado

Comando: `./gradlew :fabric:runServer --no-daemon`.

- ATC 2.1.0 carregou.
- Config schema 15 foi criada.
- Servidor chegou a `Done`.
- `/atc status`, CAPS, preset, help e rejeição de `/atc config particles enable` foram verificados.
- Encerramento com `stop` salvou os mundos e terminou limpo.

### Forge 1.19.2 dedicado

Comando: `./gradlew :forge:runServer --no-daemon`.

- ATC 2.1.0 inicializou.
- Config schema 15 foi criada.
- Servidor chegou a `Done`.
- A mesma amostra de comandos foi validada.
- Encerramento foi limpo.

O ambiente de desenvolvimento Forge corrigiu automaticamente uma entrada duplicada `disableOptimizedDFU` do `fml.toml` gerado. É um warning local de configuração do Forge, sem crash ou erro do ATC.

### Clientes Fabric e Forge 1.19.2

Os dois clientes de desenvolvimento carregaram o ATC, inicializaram áudio/atlases e chegaram à tela inicial. O primeiro download de asset Fabric precisou de uma repetição por falha transitória de rede. Erros de autenticação/Realms são esperados no perfil de desenvolvimento offline.

Os clientes foram encerrados manualmente após a tela inicial; isso valida carregamento e mixins, mas não substitui um teste multiplayer interativo.

Depois da descoberta da regressão de preview no Forge, foi executado um segundo teste na instância Prism Forge 1.19.2 com Forge 43.5.0 e o JAR final corrigido. Com debug habilitado, a frase `typing without enter test` foi digitada lentamente sem Enter e produziu zero scans. Uma segunda edição com Backspace e cancelamento por Esc também produziu zero scans. O envio da primeira frase com um único Enter produziu exatamente uma entrada `Chat from` e ela foi executada na `Server thread`. O mundo foi fechado com salvamento limpo.

## Limitações restantes

- Não foi automatizada uma sessão multiplayer real com cliente vanilla, cliente com ATC e dois jogadores simultâneos.
- Não foi automatizada a observação visual de path particles por jogador.
- Movimentação, prioridade e alcance de cada família de mob precisam da checklist in-game.
- Terrain Muffling e entidades de outros mods precisam de validação visual/manual.
- O commit remoto que corrigia a tabela do README foi revisado e sua correção foi preservada antes da publicação da branch.

Consulte `RELEASE_TEST_2.1.0_1.19.2.md` para o sign-off manual.

## Artefatos finais

| Artefato | SHA-256 |
|---|---|
| `Attract-To-Chat-1.19.2-Fabric-2.1.0.jar` | `2abda3502f5d1cb35ed848e52fc18418132eb239b3bfa7dafe787300a680a530` |
| `Attract-To-Chat-1.19.2-Forge-2.1.0.jar` | `50ecd8a7ff0519738d078e3528df4611389f0491f16d99d45236aa1f124a981e` |

Somente os JARs runtime remapeados são artefatos de publicação. `sources`, `dev-shadow`, intermediários e outputs não remapeados não fazem parte do pacote de release.

## Arquivos alterados

### Core/common

- `AttractToChat.java`
- `AtcCommand.java`
- `AttractToChatConfig.java`
- `AtcEngine.java`
- `MoveToSoundGoal.java`
- `GroundTargetResolver.java`
- `ClientPresence.java`
- `ServerTranslations.java`
- `ServerPlayerMixin.java`
- `attracttochat.common.mixins.json`
- `en_us.json`, `pt_br.json`, `es_es.json`
- `common/build.gradle`
- testes em `common/src/test`

### Fabric

- `AtcFabricMod.java`
- `AtcFabricClient.java`
- `FabricPlatformHelper.java`
- `fabric.mod.json`
- removidos o mixin de chat redundante e seu descritor Fabric.

### Forge

- `AtcForgeClient.java`
- `ForgePlatformHelper.java`
- `mods.toml`
- `attracttochat.forge.mixins.json`

### Projeto e documentação

- `.gitignore`
- `README.md`
- `CHANGELOG.md`
- `ATC-2.1.0-1.19.2-PARITY-AUDIT.md`
- `RELEASE_TEST_2.1.0_1.19.2.md`

## Estado Git

Nenhum force-reset, descarte ou exclusão de alteração do usuário foi realizado. O conjunto foi preparado para publicação somente após autorização do proprietário.

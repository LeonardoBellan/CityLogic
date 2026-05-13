# 1

Agisci come un Software Architect. Sto lavorando a un progetto per un simulatore di città in cui l'utente deve costruire edifici all'interno di una mappa (griglia). Devo stilare il Modello di Dominio (Domain Model).

Leggi le seguenti User Stories e i relativi Criteri di Accettazione. Estrapola i le principali classi, i loro attributi base e le relazioni con le relative molteplicità.

1 - "Come giocatore, voglio che il sistema mi permetta di posizionare edifici sulla mappa a griglia affinché io possa definire la struttura della mia città.
Descrizione
Limiti della griglia: Il posizionamento degli edifici deve avvenire strettamente all’interno delle coordinate della griglia 20x20
Spazio libero: Il posizionamento deve avvenire su una casella in cui non sono presenti altri edifici.
Regole di adiacenza: Il posizionamento degli edifici deve avvenire INSERIRE REGOLE.
Costo di costruzione: Al momento della costruzione il costo dell’edificio deve essere sottratto dal budget del giocatore.
Feedback visivo: Durante il posizionamento, il sistema deve mostrare un’anteprima dell’edificio (verde valido, rosso non valido)."

2 - "Come giocatore, voglio che gli edifici consumino risorse e generino effetti (positivi o negativi) sull'area circostante, affinché la gestione della città richieda pianificazione e strategia.
Descrizione
Consumo e manutenzione: Gli edifici attivi devono consumare energia e acqua o sottrarre costi di mantenimento.
Produzione: Gli edifici specifici (es. residenziali, commerciali) devono generare periodicamente popolazione o tasse.
Effetti ad area: Gli edifici di servizio devono fornire bonus alle celle entro un determinato raggio.
Inquinamento: Gli edifici industriali devono generare inquinamento nelle caselle adiacenti.
Stato di inattività: Se un edificio non riceve le risorse necessarie deve smettere di funzionare e segnalare visivamente il problema."

3 - "Come giocatore, voglio poter demolire edifici presenti nella mappa affinché io possa liberare spazio e riorganizzare la struttura della mia città.
Descrizione
Selezione valida: Lo strumento di demolizione deve avere effetto solamente su celle attualmente occupate da un edificio.
Liberazione celle: Le celle occupate dall’edificio demolito devono immediatamente tornare disponibili per il posizionamento di altri edifici.
Rimozione effetti: La demolizione di un edificio deve annullare immediatamente gli effetti ed i consumi legati a quell’edificio.
Icona selezionabile: Presenza di un’icona che permette di attivare/disattivare lo strumento di demolizione."

In qualità di Software Architect, ho analizzato le tue User Stories per delineare un **Domain Model** solido, estensibile e orientato alla logica di business di un simulatore urbano.

Ecco la scomposizione del dominio in classi, attributi e relazioni.

---

## 1. Classi Principali e Attributi

### **Player (Giocatore)**

Rappresenta l'entità che gestisce la città e possiede le risorse.

- **Attributi:**
- `budget`: Decimal (Il capitale finanziario disponibile).
- `totalPopulation`: Integer (Somma degli abitanti generati dagli edifici).

### **Grid (Griglia)**

Il contenitore spaziale del gioco.

- **Attributi:**
- `width`: Integer (Default: 20).
- `height`: Integer (Default: 20).

- **Metodi Chiave:** `isCellOccupied(x, y)`, `isValidPosition(x, y)`.

### **Cell (Cella/Casella)**

L'unità minima della mappa.

- **Attributi:**
- `coordinate`: Point (x, y).
- `pollutionLevel`: Float (Livello di inquinamento accumulato).
- `isOccupied`: Boolean.

### **Building (Edificio) - Classe Base**

Classe astratta o base che definisce le proprietà comuni.

- **Attributi:**
- `id`: UUID.
- `name`: String.
- `constructionCost`: Decimal.
- `maintenanceCost`: Decimal.
- `resourceConsumption`: ResourcePacket (Acqua, Energia).
- `isActive`: Boolean (Stato operativo basato sulle risorse).
- `footprint`: Shape (Dimensioni in celle).

### **Specific Buildings (Sottoclassi)**

- **Residential/Commercial:** Attributi per `taxRate` e `populationCapacity`.
- **Industrial:** Attributo `pollutionOutput`.
- **Service (Scuole, Polizia):** Attributi `effectRadius` e `bonusType`.

---

## 2. Relazioni e Molteplicità

| Relazione             | Molteplicità | Descrizione                                                                             |
| --------------------- | ------------ | --------------------------------------------------------------------------------------- |
| **Player - Grid**     | 1 : 1        | Un giocatore gestisce una singola griglia di gioco.                                     |
| **Grid - Cell**       | 1 : 400      | La griglia 20x20 è composta esattamente da 400 celle.                                   |
| **Cell - Building**   | 0..1 : 0..1  | Una cella può ospitare al massimo un edificio; un edificio occupa una o più celle.      |
| **Building - Effect** | 1 : 0..\*    | Un edificio può generare multipli effetti (bonus/inquinamento) sulle celle circostanti. |

---

---

## 3. Logiche di Dominio Emergenti (Business Rules)

Dalle tue User Stories emergono tre sistemi core che il modello deve supportare:

### **A. Il Sistema di Validazione (Placement Engine)**

Prima di istanziare un `Building`, un servizio di validazione deve verificare:

1. **Bounds Check:** Le coordinate devono essere $\in [0, 19]$.
2. **Collision Check:** `Cell.isOccupied` deve essere `false`.
3. **Financial Check:** `Player.budget >= Building.constructionCost`.

### **B. Il Sistema di Stato (Resource & Effect Engine)**

Il modello non è statico. Esiste un ciclo (Tick) che processa:

- **Consumo:** Se il `Player` o la rete non forniscono `ResourcePacket`, `Building.isActive` diventa `false`.
- **Propagazione:** Gli edifici "Industrial" applicano un incremento al `pollutionLevel` delle `Cell` adiacenti. Gli edifici "Service" applicano bonus alle `Cell` entro l' `effectRadius`.

### **C. Il Sistema di Demolizione (Deconstruction)**

La demolizione è l'inverso del posizionamento:

- Rilascia il flag `isOccupied` sulle celle.
- **Importante:** Deve triggerare un ricalcolo immediato dell'area di influenza (rimozione inquinamento/bonus) per evitare che gli effetti persistano senza l'edificio sorgente.

---

### Prossimo Step Consigliato

Per procedere con lo sviluppo, suggerisco di definire un **Pattern Strategy** per gestire i diversi tipi di "Effetti ad area" (Inquinamento vs Bonus Felicità), così da poter aggiungere nuovi tipi di edifici in futuro senza modificare la logica core della Griglia.

---

# 2

Il modello che hai generato è troppo orientato al software e assomiglia a un Design Class Model. A me serve un design di modello concettuale.

1. Rimuovi le menzioni ai metodi e ai tipi di dato (integer, string...)
2. Identifica solamente gli attributi essenziali di ogni oggetto.

Hai ragione. Per un **Domain Model (Modello Concettuale)**, dobbiamo concentrarci esclusivamente sulla comprensione dei concetti del dominio e sulle loro relazioni, eliminando ogni dettaglio implementativo o scelta tecnologica.

Ecco il modello semplificato e focalizzato sulla logica del "mondo reale" del tuo simulatore:

---

## Modello Concettuale del Dominio

### **Città / Griglia**

Rappresenta l'ambiente fisico del gioco.

- **Attributi:** Dimensioni, Limiti geografici.

### **Cella**

L'unità di spazio minima che compone la città.

- **Attributi:** Posizione, Stato di occupazione (libera/occupata), Livello di inquinamento, Bonus di servizio ricevuto.

### **Edificio**

Qualunque struttura costruibile sulla mappa.

- **Attributi:** Nome, Costo di costruzione, Costo di mantenimento, Ingombro (spazio occupato), Stato operativo (attivo/inattivo).

### **Risorsa**

Ciò che viene consumato o prodotto dagli edifici per funzionare.

- **Attributi:** Tipo (Acqua, Energia, Denaro), Quantità.

### **Effetto Ambientale**

L'impatto che un edificio ha sullo spazio circostante.

- **Attributi:** Tipo (Bonus/Malus), Raggio d'azione, Intensità.

### **Giocatore**

L'entità che prende decisioni e gestisce l'economia.

- **Attributi:** Risorse accumulate (Budget), Popolazione totale.

---

## Relazioni tra i Concetti

| Entità A      | Relazione            | Entità B    | Descrizione                                                                    |
| ------------- | -------------------- | ----------- | ------------------------------------------------------------------------------ |
| **Città**     | è composta da        | **Celle**   | Una Città aggrega molteplici Celle (20x20).                                    |
| **Edificio**  | occupa               | **Celle**   | Un Edificio è posizionato su una o più Celle specifiche.                       |
| **Edificio**  | consuma/produce      | **Risorse** | Un Edificio richiede Risorse per restare attivo e può generarne altre (Tasse). |
| **Edificio**  | genera               | **Effetti** | Un Edificio attivo proietta Effetti sulle Celle vicine.                        |
| **Cella**     | subisce              | **Effetti** | Una Cella accumula gli impatti (es. Inquinamento) degli Edifici adiacenti.     |
| **Giocatore** | possiede             | **Risorse** | Il Giocatore gestisce il budget per costruire o demolire.                      |
| **Giocatore** | costruisce/demolisce | **Edifici** | Il Giocatore agisce direttamente sugli Edifici nella Città.                    |

---

## Logiche Concettuali Chiave

- **Validità di Posizionamento:** Un Edificio può esistere solo se le Celle che intende occupare sono entro i confini della Città e non sono già impegnate da un altro Edificio.
- **Dipendenza Operativa:** L'efficacia di un Edificio (la sua capacità di generare Effetti o Popolazione) è strettamente legata alla disponibilità di Risorse di mantenimento.
- **Rimozione Totale:** La demolizione di un Edificio comporta la cessazione immediata di ogni suo Consumo e la scomparsa di ogni Effetto generato sulle Celle circostanti.

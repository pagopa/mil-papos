# POS PA

## Glossario

### Soluzione
Indica la totalità dei POS assegnati ad un Ente Creditore, quindi una soluzione possiamo affermare che rappresenta un Ente Creditore (payee)

### Sistema
Indica questo servizio.

### Amministratore
Indica l'amministratore del sistema.

### POS Service Provider
Indica il soggetto che gestisce i terminali POS.

## User Stories

### Amministratore del sistema
#### Soluzione
1. **Come** Amministratore **voglio** creare una soluzione **così da** configurare il sistema per un Ente Creditore. `POST /payees`
2. **Come** Amministratore **voglio** cancellare una soluzione **così da** gestire la dismissione di un Ente Creditore. `DELETE /payees/{payeeCode}`
3. **Come** Amministratore **voglio** aggiornare una soluzione **così da** gestire le modifiche alla configurazione del sistema per un Ente Creditore. `PUT /payees/{payeeCode}` 
4. **Come** Amministratore **voglio** ottenere i dettagli di una soluzione **così da** consultare la configurazione di un Ente Creditore. `GET /payees/{payeeCode}`
5. **Come** Amministratore **voglio** ottenere l'elenco delle soluzioni **così da** eseguire una operazione di cancellazione, aggiornamento o recupero dei dettagli di una di esse. `GET /payees`

### POS Service Provider
#### POS
6. **Come** POS Service Provider **voglio** registrare un POS in una soluzione **così da** renderlo utilizzabile dal sistema. `POST /payees/{payeeCode}/terminals`
7. **Come** POS Service Provider **voglio** cancellare un POS in una soluzione **così da** gestire la dismissione di un POS. `DELETE /payees/{payeeCode}/terminals/{terminalUuid}`
8. **Come** POS Service Provider **voglio** aggiornare i dati di un POS in una soluzione **così da** gestire le modifiche alla configurazione dello stesso. `PUT /payees/{payeeCode}/terminals/{terminalUuid}`
9. **Come** POS Service Provider **voglio** ottenere i dettagli di un POS **così da** consulare la configurazione dello stesso. `GET /payees/{payeeCode}/terminals/{terminalUuid}`
10. **Come** POS Service Provider **voglio** ottenere l'elenco dei POS in una soluzione **così da** eseguire una operazione di cancellazione, aggiornamento o recupero dei dettagli di un POS. `GET /payees/{payeeCode}/terminals`
11. **Come** POS Service Provider **voglio** eseguire il bulk loading dei POS in una soluzione **così da** renderli utilizzabili dal sistema. `POST /payees/{payeeCode}/terminals/bulkload`
12. **Come** POS Service Provider **voglio** ottenere l'elenco delle soluzioni che adoperano il mio servizio di Gestore POS **così da** poter eseguire un'operazione (vedi 6-11) su una di esse. `GET /payees?pspId={pspId}`
13. **Come** POS Service Provider **voglio** ottenere i dettagli di una soluzione **così da** eseguire eventuali analisi. `GET /payees/{payeeCode}`

### Ente Creditore
14. **Come** Ente Creditore **voglio** associare un POS ad una cassa **così da** poter creare la posizione per lo scambio avviso remoto. `PATCH /payees/{payeeCode}/terminals/{terminalUuid}`
15. **Come** Ente Creditore **voglio** ottenere l'elenco dei POS associati ad una cassa **così** da poter creare la posizioe per lo scambio avviso remoto. `GET /payees/{payeeCode}/terminals?workstationName={workstationName}`
16. **Come** Ente Creditore **voglio** ottenere l'elenco dei POS associati alla mia soluzione **così da** selezionarne uno per associarlo ad una cassa. `GET /payees/{payeeCode}/terminals`
17. **Come** Ente Creditore **voglio** creazione una posizione per un POS **così da** innescare lo scambio avviso remoto. `POST /payees/{payeeCode}/terminals/{terminalUuid}/transactions`
18. **Come** Ente Creditore **voglio** cancellare una posizione per lo scambio importo remoto **così da** gestire eventuali errori. `DELETE /payees/{payeeCode}/terminals/{terminalUuid}/transactions/{transactionId}`
19. **Come** Ente Creditore **voglio** ottenere i dettagli di una posizione per lo scambio importo remoto **così da** conoscerne il suo stato. `GET /payees/{payeeCode}/terminals/{terminalUuid}/transactions/{transactionId}`

## Entità

### Soluzione (Payee)
```json
{
	"payeeUuid": "<ID della soluzione per il Sistema>",
	"payeeCode": "<Codice Fiscale dell'Ente Creditore>",
	"description": "<Descrizione dell'Ente Creditore>",
	"office": "<Eventuale Ufficio>",
	"pspId": "<Codice Fiscale del POS Service Provider (es. WorldLine)>"
	"southConfig": {
		.
		.
		.
	}
}
```

### POS (terminal)
```json
{
	"terminalUuid": "<ID della soluzione per il Sistema>",
	"terminalId": "<Codice Fiscale dell'Ente Creditore>",
	"payeeUuid": "<Descrizione dell'Ente Creditore>",
	"workstations": [
		"<workstation name #1>",
		"<workstation name #2>",
		.
		.
		.
		"<workstation name #n>"
	]
}
```

### Transaction
```json
{
	"transactionUuid": "<ID della posizione>",
	"terminalUuid": "<ID della soluzione per il Sistema>",
	"insertTimestamp": "<timestamp di inserimento>",
	"lastUpdateTimestamp": "<timestamp dell'ultimo aggiornamento>",
	"status": "OPEN|CLOSED",
	"paymentNotice": {
		"noticeNumber": "<Numero dell'avviso pagoPA>",
		"fiscalCode": "<Codice Fiscale dell'Ente Creditore>",
		"amount": <Importo dell'Avviso pagoPA> 
	},
	"receipt": {
		"timestamp": "<timestamp del pagamento>",
		"amount": <Importo effettivamente pagato>
	}
}
```
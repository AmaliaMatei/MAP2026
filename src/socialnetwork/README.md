# LABORATORUL 3
## 1. Descrierea proiectului

Proiectul reprezinta o aplicatie Java pentru gestionarea unei retele de socializare.

Aplicatia permite:
- Adaugarea si stergerea utilizatorilor.
- Adaugarea si stergerea relatiilor de prietenie.
- Afisarea utilizatorilor si a prieteniilor.
- Determinarea numarului de comunitati (componente conexe).
- Determinarea celei mai sociabile comunitati, pe baza celui mai lung drum simplu din graf.

Utilizatorii sunt identificati in consola dupa nume si prenume. Daca doua persoane au acelasi nume complet, se foloseste adresa de email pentru diferentiere. ID-urile sunt folosite intern.

Laboratorul 3 continua implementarea Laboratorului 2, adaugand persistenta datelor in fisiere text si folosirea sablonului Factory.

## 2. Arhitectura proiectului

Aplicatia foloseste o arhitectura stratificata, organizata in urmatoarele pachete:

- domain - clasele User, Friendship, Entity si Tuple.
- repository - interfata Repository si implementarile pentru memorie, fisiere si scheletele pentru baza de date.
- factory - RepositoryFactory si RepositoryType.
- service - UserService, FriendshipService si CommunityService.
- validation - interfata Validator si validatorii pentru utilizatori si prietenii.
- exceptions - exceptiile personalizate.
- ui - interfata in consola.
- Main - initializarea componentelor si pornirea aplicatiei.

Fiecare strat are responsabilitati separate.

## 3. Persistenta datelor

Aplicatia suporta trei tipuri de persistenta: MEMORY, FILE si DATABASE.

Tipul de persistenta este ales din fisierul src/main/resources/config.properties.

In modul FILE sunt folosite doua fisiere:

- data/utilizatori.txt
- data/prietenii.txt

Formatul extins folosit pentru utilizatori este:

id;prenume;nume;email;varsta;oras

Exemplu:
1;Ana;Popescu;ana@gmail.com;21;Cluj-Napoca

Am pastrat datele suplimentare din Laboratorul 2, pentru a putea identifica utilizatorii dupa nume si email.

Formatul pentru prietenii este:

idUtilizator1;idUtilizator2;dataPrieteniei

Exemplu:
1;2;2026-10-19T10:30

Data este reprezentata folosind LocalDateTime.

Citirea si scrierea folosesc NIO.2, UTF-8 si try-with-resources.

## 4. AbstractFileRepository si Template Method

Clasa AbstractFileRepository<ID, E extends Entity<ID>> extinde InMemoryRepository si implementeaza logica generala pentru citirea si scrierea fisierelor.

La pornire sunt incarcate datele existente, iar dupa operatiile save, delete si update reusite fisierul este actualizat.

Subclasele UtilizatorFileRepository si PrietenieFileRepository implementeaza doar metodele:

- extractEntity(String line)
- createEntityAsString(E entity)

Astfel, codul de citire si scriere nu este duplicat.

La incarcarea datelor se foloseste super.save() pentru a evita rescrierea fisierului in timpul citirii.

## 5. RepositoryFactory

RepositoryFactory implementeaza sablonul Simple Factory.

Factory-ul citeste fisierul config.properties folosind java.util.Properties si creeaza repository-uri in functie de repository.type.

Valorile acceptate sunt:

- MEMORY - InMemoryRepository.
- FILE - UtilizatorFileRepository si PrietenieFileRepository.
- DATABASE - UtilizatorDbRepository si PrietenieDbRepository.

Factory-ul creeaza ambele repository-uri folosind acelasi tip de persistenta.

Service-urile depind de interfata Repository, nu de implementarile concrete.

Pentru trecerea la baza de date, in Laboratorul 4 trebuie implementate operatiile JDBC din clasele UtilizatorDbRepository si PrietenieDbRepository. Ulterior, repository.type poate fi schimbat in DATABASE, fara modificarea service-urilor si a interfetei cu utilizatorul.

## 6. Tratarea exceptiilor

Aplicatia foloseste exceptii proprii, unchecked:

- ValidationException - pentru date invalide.
- RepositoryException - pentru erori de citire si scriere.
- DuplicateEntityException - pentru entitati deja existente.
- EntityNotFoundException - pentru entitati inexistente.

RepositoryException foloseste exception chaining, pastrand cauza originala prin constructorul cu parametrii String message si Throwable cause.

Daca fisierul nu exista, repository-ul porneste gol, iar fisierul este creat la prima salvare.

Pentru liniile corupte sau invalide am ales strategia de raportare si ignorare: aplicatia afiseaza numarul liniei si motivul erorii, apoi continua incarcarea celorlalte entitati.

Am ales aceasta varianta pentru ca o singura linie invalida sa nu blocheze incarcarea intregului fisier.

Operatiile asupra prieteniilor verifica existenta celor doi utilizatori.

## 7. Principii SOLID

SRP (Single Responsibility Principle):
Fiecare clasa are o responsabilitate clara. UserService gestioneaza utilizatori, FriendshipService gestioneaza prietenii, iar CommunityService gestioneaza algoritmii pe graf.

DIP (Dependency Inversion Principle):
Service-urile depind de interfata generica Repository, nu de clasele concrete de persistenta.

OCP (Open/Closed Principle):
Se pot introduce implementari noi ale interfetei Repository fara schimbarea logicii service-urilor. Totusi, Simple Factory necesita modificarea metodei de creare pentru fiecare tip nou de persistenta.

LSP (Liskov Substitution Principle):
Implementarile repository-urilor trebuie sa respecte contractele interfetei Repository, inclusiv regulile privind ID-urile invalide si operatiile de salvare, cautare si stergere.

## 8. Design Patterns

Factory:
RepositoryFactory centralizeaza construirea repository-urilor.

Template Method:
AbstractFileRepository defineste operatiile comune cu fisierele, iar subclasele furnizeaza conversiile specifice entitatilor.

Strategy:
Validator<T> permite folosirea unor strategii de validare diferite pentru utilizatori si prietenii.

Decorator:
La nivel conceptual, bufferingul fluxurilor de intrare/iesire foloseste ideea de decorare. In aceasta implementare este utilizata alternativa NIO.2, prin Files.newBufferedReader si Files.newBufferedWriter.

## 9. Simple Factory vs Abstract Factory

Simple Factory are avantajul unei implementari simple si al centralizarii logicii de construire a repository-urilor.

Dezavantajul este ca trebuie modificat switch-ul din RepositoryFactory atunci cand introducem un nou tip de persistenta, ceea ce limiteaza respectarea principiului OCP.

Abstract Factory ar permite definirea unor fabrici separate, precum FileRepositoryFactory si DbRepositoryFactory.

Avantajul Abstract Factory este extensibilitatea mai buna. Dezavantajul este numarul mai mare de clase si complexitatea suplimentara.

Pentru acest laborator am ales Simple Factory deoarece este suficient pentru cele trei tipuri de persistenta cerute.

## 10. Validarea datelor

Validarea formatului datelor se realizeaza folosind interfata Validator<T>.

Repository-urile valideaza entitatile inainte de salvare si la incarcarea din fisier.

UserService verifica suplimentar unicitatea emailului, iar validatorul prieteniilor verifica existenta utilizatorilor.

Aceasta separare permite reutilizarea validarilor indiferent de tipul de persistenta ales.

## 11. Rularea aplicatiei

Aplicatia se porneste executand clasa Main.java.

Pentru folosirea fisierelor text se seteaza:

repository.type=FILE

in config.properties.

La pornire se incarca datele existente si se afiseaza meniul din consola.

Pentru testarea persistentei:
1. Se adauga un utilizator.
2. Se verifica salvarea lui in utilizatori.txt.
3. Se inchide aplicatia.
4. Se reporneste aplicatia.
5. Se verifica existenta utilizatorului adaugat anterior.

Se pot testa si schimbarea tipului de persistenta, fisierul lipsa si introducerea unei linii corupte.

# Laboratorul 4
## 1. Descrierea proiectului

Laboratorul 4 reprezinta refactorizarea aplicatiei "Retea de socializare", realizata in laboratoarele 2 si 3.

Scopul laboratorului este utilizarea facilitatilor Java 8: interfete functionale, expresii lambda, referinte la metode, Stream API, Optional si metode default in interfete.

Functionalitatile aplicatiei au fost pastrate:

- Adaugarea si stergerea utilizatorilor.
- Adaugarea si stergerea prieteniilor.
- Afisarea utilizatorilor si prieteniilor.
- Determinarea numarului de comunitati.
- Determinarea celei mai sociabile comunitati.
- Persistenta datelor in fisiere text.

Pentru comunitatea cea mai sociabila se pastreaza criteriul din implementarea anterioara: lungimea celui mai lung drum simplu din componenta conexa.

## 2. Refactorizarea buclelor clasice folosind Stream API

| Clasa | Metoda | Inainte - Laboratorul 3 | Dupa - Laboratorul 4 |
|---|---|---|---|
| UserService | nextId() | Bucla for pentru determinarea ID-ului maxim | stream(), map(), max(), orElse() |
| UserService | getFriendsOfUser() | Parcurgerea prieteniilor si construirea listei | filter(), flatMap(), distinct(), map(), sorted(), collect() |
| UserService | removeUser() | Bucla pentru identificarea prieteniilor utilizatorului | filter(), map(), collect(), forEach() |
| CommunityService | buildGraph() | Parcurgerea utilizatorilor si prieteniilor pentru construirea grafului | map(), filter(), flatMap(), collect(), toMap() |
| CommunityService | getCommunities() | Parcurgerea nodurilor pentru componente conexe | stream(), groupingBy(), DFS recursiv |
| CommunityService | longestPathLength() | Parcurgerea nodurilor comunitatii | mapToInt(), max(), orElse() |
| ConsoleUI | showUsers() | Bucla for pentru afisarea utilizatorilor | stream(), sorted(), forEach() |
| ConsoleUI | run() | Bucla while pentru meniul interactiv | Stream.generate(), anyMatch() |
| AbstractFileRepository | loadData() | Citirea liniilor printr-o bucla | Files.lines(), collect(), IntStream.range() |
| AbstractFileRepository | writeToFile() | Construirea liniilor pentru scriere | stream(), map(), collect(), Files.write() |

Operatiile intermediare, precum filter, map, flatMap si sorted, construiesc pipeline-ul de procesare.

Operatiile terminale, precum collect, count, max, anyMatch si forEach, declanseaza evaluarea stream-ului.

Listele rezultate sunt construite cu Collectors, fara adaugarea elementelor intr-o lista exterioara din forEach.

## 3. Interfete functionale si expresii lambda

Au fost utilizate urmatoarele interfete functionale:

**Validator<T>**

Interfata functionala proprie, adnotata cu @FunctionalInterface, care contine metoda abstracta validate(T entity).

A fost adaugata metoda default andThen(Validator<T> other), care permite compunerea regulilor de validare.

In Main.java, validatorul existent este combinat cu o regula lambda suplimentara care interzice caracterele ce pot deteriora formatul fisierelor text.

Regula este adaugata fara o clasa noua si fara modificarea validatorului existent pentru aceasta regula.

**Predicate<T>**

Folosit in Repository.java pentru metoda default findAll(Predicate<E> filter).

Clasa CriteriiUtilizator contine metode statice care construiesc criterii de cautare dupa nume, oras, varsta si existenta prieteniilor.

Predicatele pot fi combinate folosind and(), or() si negate().

**Supplier<T>**

Folosit in RepositoryFactory pentru crearea repository-urilor.

Fiecare RepositoryType este asociat cu un Supplier intr-un EnumMap. Repository-ul este construit doar cand se apeleaza get() pentru tipul selectat.

**Runnable**

Folosit in ConsoleUI pentru asocierea optiunilor meniului cu actiunile corespunzatoare.

Map<String, Runnable> inlocuieste instructiunea switch din procesarea meniului.

**Function<T, R> si Comparator<T>**

Function este folosit prin operatiile de transformare map(), inclusiv prin referinte la metode.

Comparator este folosit pentru sortarea utilizatorilor si pentru determinarea comunitatii celei mai sociabile.

## 4. Referinte la metode

In proiect sunt utilizate cele patru categorii de referinte la metode:

| Tip | Exemplu |
|---|---|
| Metoda statica | Objects::nonNull |
| Metoda de instanta a unui obiect anume | System.out::println |
| Metoda de instanta a unui obiect oarecare | User::getLastName |
| Constructor | TreeSet::new |

Alte exemple: User::getId, Friendship::getId, this::readOption, this::executeOption, friendships::delete si this::createEntityAsString.

Referintele la metode sunt preferate expresiilor lambda atunci cand lambda doar apeleaza o metoda existenta.

## 5. Decizia privind Optional in Repository

Am ales sa pastram Optional<E> ca tip de retur pentru Repository.findOne(ID id), deoarece aceasta abordare exista deja in laboratorul 3.

Optional exprima explicit posibilitatea ca o entitate sa nu existe.

In InMemoryRepository, cautarea utilizeaza Optional.ofNullable().

In service sunt utilizate orElseThrow(), orElse() si alte operatii sigure pentru prelucrarea rezultatelor.

Aceasta abordare evita returnarea valorii null si apelarea nesigura a metodei get().

## 6. Decizia privind repository-urile pe fisier

Am pastrat sablonul de proiectare Template Method.

Clasa AbstractFileRepository gestioneaza operatiile comune de citire, scriere si persistenta.

Clasele UtilizatorFileRepository si PrietenieFileRepository implementeaza metodele de conversie dintre o linie text si o entitate.

Aceasta solutie a fost aleasa deoarece exista deja in laboratorul 3 si permite pastrarea structurii proiectului fara modificari arhitecturale inutile.

Nu am trecut la un repository generic bazat pe Function<String, E> si Function<E, String>, deoarece Template Method separa deja responsabilitatile intr-un mod clar.

## 7. Persistenta datelor

Citirea fisierelor se realizeaza folosind Files.lines(path, StandardCharsets.UTF_8), intr-un bloc try-with-resources.

Pentru numerotarea liniilor se foloseste IntStream.range(), astfel incat liniile invalide sa poata fi raportate cu numarul lor.

Pentru scriere, entitatile sunt transformate in linii text prin stream(), map() si collect(Collectors.toList()), apoi sunt scrise cu Files.write().

Scrierea se face initial intr-un fisier temporar, urmata de inlocuirea fisierului original.

In cazul unei erori de scriere, repository-ul restaureaza starea anterioara din memorie.

Formatul fisierelor utilizatori.txt si prietenii.txt nu a fost modificat.

Fisierele inexistente sunt tratate conform comportamentului anterior, iar liniile corupte sunt raportate.

## 8. Comunitati si algoritmi pe graf

Reteaua sociala este modelata ca un graf neorientat.

Utilizatorii reprezinta nodurile, iar prieteniile reprezinta muchiile.

Comunitatile sunt componentele conexe ale grafului si sunt determinate folosind DFS recursiv.

Vecinii sunt prelucrati folosind stream-uri.

Pentru determinarea comunitatii celei mai sociabile se calculeaza lungimea celui mai lung drum simplu pentru fiecare componenta conexa.

Selectarea comunitatii se face cu max() si Comparator.comparingInt().

Backtracking-ul este pastrat pentru explorarea drumurilor simple, deoarece este necesara urmarirea nodurilor vizitate pe fiecare traseu.

## 9. Bucle pastrate si justificare

In pachetele domain, repository, service si UI nu sunt utilizate bucle clasice for, for-each, while sau do-while in codul refactorizat prezentat.

In algoritmul celui mai lung drum simplu este pastrata recursivitatea cu backtracking.

Multimea visited este actualizata la intrarea si la iesirea din fiecare apel recursiv pentru a explora drumuri alternative.

Aceasta abordare este justificata de natura algoritmului si de necesitatea pastrarii comportamentului din laboratorul 3.

Nu este folosit parallelStream(), deoarece algoritmii de graf folosesc stare mutabila locala, iar paralelizarea nu ofera avantaje pentru dimensiunea proiectului.

## 10. Gradle si configurarea aplicatiei

Proiectul foloseste Gradle pentru compilare si rulare.

Fisierele build.gradle si settings.gradle definesc configuratia proiectului.

Codul este configurat pentru compatibilitate Java 8.

Comenzi:

- gradle clean build - compileaza proiectul si ruleaza testele Gradle configurate.
- gradle run - porneste aplicatia.

Tipul repository-ului este ales din src/main/resources/config.properties.

Sunt disponibile MEMORY, FILE si DATABASE.

Pentru DATABASE, implementarile raman schelete care arunca UnsupportedOperationException. Persistenta in baza de date va fi implementata in laboratorul 5.

## 11. Verificarea functionalitatilor

Pentru datele initiale din laboratorul 3, rezultatele de referinta sunt:

- 9 utilizatori.
- 6 prietenii.
- 3 comunitati.
- Comunitatea cea mai sociabila contine utilizatorii cu ID-urile 1, 2, 3, 4 si 5.
- Cel mai lung drum simplu din comunitatea selectata are 4 muchii.

Trebuie verificate si adaugarea/stergerea utilizatorilor, adaugarea/stergerea prieteniilor, persistenta dupa repornire, fisierul lipsa si raportarea liniilor corupte.

## 12. Versionarea proiectului

Pentru versionarea codului este folosit Git.

Repository-ul trebuie sa includa codul sursa, fisierele de configurare, folderul data/, build.gradle, settings.gradle si documentatia.

Fisierele compilate si directoarele generate de Gradle sau IntelliJ sunt excluse prin .gitignore.

Istoricul commit-urilor documenteaza evolutia proiectului si refactorizarea din laboratorul 4.

## 13. Concluzie

Laboratorul 4 urmareste modernizarea implementarii aplicatiei prin folosirea facilitatilor Java 8.

Au fost introduse interfete functionale, lambda, Predicate, Supplier, referinte la metode, Stream API si Optional, pastrand arhitectura de baza si persistenta in fisiere text.

Scopul refactorizarii este obtinerea unui cod mai declarativ, reutilizabil si mai usor de intretinut, fara modificarea functionalitatilor existente.
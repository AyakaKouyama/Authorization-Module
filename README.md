# System automatycznego pobierania opłat na parkingach z nim połączonych - backend

## Kursy
Przy realizacji projektu przydatne mogą być poniższe kursy, które omawiają podstawy potrzebnych zagadnień aczkolwiek nie są one bezwględnie konieczne.
  - [JPA](https://www.tutorialspoint.com/jpa/jpa_orm_components.htm)
   - [REST API - part I](http://www.samouczekprogramisty.pl/rest-web-service-z-java-ee-czesc-1/)
  - [REST API - part II](http://www.samouczekprogramisty.pl/rest-web-service-z-java-ee-czesc-2//)

## Projektowanie modułu
Zgodnie z tym, co przedstawiliśmy podczas prezentacji każdy moduł logiki biznesowej ma być zaprojektowany według wzorca ECB (Entity, Control, Boundary). Więcej o tym wzorcu można przeczytać [tutaj](https://it-consulting.pl/autoinstalator/wordpress/2012/10/18/wzorzec-analityczny-boundary-control-entity/).

Entity to encje, czyli klasy reprezentujące model biznesowy, które będą mapowane przez JPA (specyfikację naszego ORM'a) na tabele bazy danych. Na diagramach klas oznaczamy je stereotypem `<<entity>>`. Zawierają one jedynie pola, konstruktory, gettery, settery, przeciążone metody dziedziczone z klasy Object oraz ewentualnie metody oznaczone adnotacjami `@PrePersist`, `@PreRemove` itp. Dodatkowo przy implementacji będą one oznaczane adnotacjami JPA, żeby nasz ORM wiedział jak ma mapować je na tabele bazy danych. 

Przykładowa encja `User` przedstawiona poniżej zawiera 4 pola oznaczone adnotacjami, które zostaną zmapowane na 4 atrybuty relacji `user` w bazie danych. Warto nadmienić, iż wszystkie pola encji powinny być typów obiektowych (Long, Boolean) a nie prostych jak int czy float, ponieważ wtedy przy wystąpieniu wartości `null` w bazie sypnie nam błędem. Każda encja powinna mieć też przeciążone metody `equals` i `hashCode` zależne jedynie od klucza (w tym wypadku od id).
Adnotacja `@NamedQuery` zostanie wyjaśniona w dalszej części.
```java
@Entity
@Table(name = "\"user\"")
@NamedQueries({
		@NamedQuery(name = "User.getByLogin",
				query = "SELECT u FROM User u WHERE u.login = :login")
})
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false)
	private Long id;

	@Column(name = "login", length = 64, unique = true, nullable = false)
	private String login;

	@Column(name = "password", length = 128, nullable = false)
	private String password;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	public User() {
		//default public constructor
	}

	//getters and setters omitted for brevity

	@PrePersist
	public void prePersist() {
		//will be done before adding new entity to the database
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof User)) return false;
		User user = (User) o;
		return Objects.equals(getId(), user.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
```

Control to klasy, które przeprowadzają operacje na obiektach  encji - w naszym przypadku będą to repozytoria i serwisy. Na diagramach klas oznaczamy je stereotypem `<<control>>`. Repozytorium ma udostępniać metody, które przeprowadzają podstawowe operacji na obiektach encji, więc dla każdej klasy encyjnej musi być zdefiniowane oddzielne repozytorium (chyba że jest jakaś relacja 1:1 i tworzenie tego repozytorium nie ma sensu). Każde repozytorium powinno być projektowane jako interfejs a następne implementowane. Bazowy interfejs repozytorium oraz bazowa klasa abstrakcyjna repozytorium zostały przez nas przygotowane do wykorzystania. Przykład zamieszczam poniżej.

Bazowy interfejs repozytorium:
```java
interface Repository<E, ID> {

	boolean existsById(ID id);
	Optional<E> getById(ID id);
	List<E> getAll();
	List<E> getAll(String RSQLQuery, int page, int limit, List<SortOption> sortOptionList);
	void add(E entity);
	void merge(E entity);
	boolean deleteByIdIfExists(ID id);
	void refresh(E entity);
	void flush();
	void clear();
}
```

Przykładowy interfejs repozytorium dla encji `user`, który zawiera dodatkowe metody operujące na encji. Warto tu zaznaczyć iż większość zapytań może być i tak wykonana za pomocą metody `List<E> getAll(String RSQLQuery, int page, int limit, List<SortOption> sortOptionList)` przy użyciu języka RSQL czyli takiego SQL'a dla REST API (omówione w dalszej częsci), ale dla często używanych zapytań jakim jest niewątpliwie pobieranie użytkownika na podstawie loginu warto napisać taką metodę aby zwiększyć wydajność.
```java
public interface UserRepository extends Repository<User, Long> {

	Optional<User> getByLogin(String login);
	int deleteAll();
}
```

Przejdźmy teraz do implementacji interfejsu. Najpierw gotowa bazowa klasa:

```java
class AbstractRepository<E, ID extends Serializable> {

	@PersistenceContext
	private EntityManager entityManager;

	private Class<E> entityClass;

	public AbstractRepository(Class<E> entityClass) {
		this.entityClass = entityClass;
	}

	public boolean existsById(ID id) {
		return getById(id).isPresent();
	}

	public Optional<E> getById(ID id) {
		return Optional.ofNullable(entityManager.find(entityClass, id));
	}

	public List<E> getAll() {
		try {
			return searchByRSQL("", 1, 0, new ArrayList<>());
		}
		catch(Exception e) {
			throw new RSQLException("Error in RSQL syntax!", e, "", new ArrayList<>());
		}
	}

	public final List<E> getAll(String RSQLQuery, int page, int limit, List<SortOption> sortOptionList) {
		try {
			return searchByRSQL(RSQLQuery, page, limit, sortOptionList);
		}
		catch(Exception e) {
			throw new RSQLException("Error in RSQL syntax!", e, RSQLQuery, getSortFields(sortOptionList));
		}
	}

	public void add(E entity) {
		entityManager.persist(entity);
	}

	public void merge(E entity) {
		entityManager.merge(entity);
	}

	protected void deleteManaged(E entity) {
		entityManager.remove(entity);
	}

	public boolean deleteByIdIfExists(ID id) {
		Optional<E> optionalEntity = getById(id);

		if(optionalEntity.isPresent()) {
			entityManager.remove(optionalEntity.get());
			return true;
		}
		return false;
	}

	public void detach(E entity) {
		entityManager.detach(entity);
	}

	public void refresh(E entity) {
		entityManager.refresh(entity);
	}

	public void flush() {
		entityManager.flush();
	}

	public void clear() {
		entityManager.clear();
	}

	protected TypedQuery<E> createNamedQuery(String name) {
		return entityManager.createNamedQuery(name, entityClass);
	}

	protected TypedQuery<E> createNamedQuery(String name, Class<E> resultClass) {
		return entityManager.createNamedQuery(name, resultClass);
	}

	protected int executeUpdateQuery(String query) {
		return entityManager.createQuery(query).executeUpdate();
	}

	protected int executeUpdateNamedQuery(String name) {
		return entityManager.createNamedQuery(name).executeUpdate();
	}

	protected Query createQuery(String query) {
		return entityManager.createQuery(query);
	}

	private List<E> searchByRSQL(String RSQLQuery, int page, int limit, List<SortOption> sortOptionList) {
		if(page < 1)
			page = 1;

		if(limit < 0)
			limit = 0;

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> criteriaQuery = RSQLParser.parseRSQLQueryToJPACriteria(entityManager, RSQLQuery, entityClass);
		List<Order> orderList = new ArrayList<>();
		Root<?> root = criteriaQuery.getRoots().iterator().next();

		for(SortOption sortOption : sortOptionList) {
			if(sortOption.getDirection().equals(SortOption.Direction.DESC))
				orderList.add(criteriaBuilder.desc(root.get(sortOption.getValue())));
			else
				orderList.add(criteriaBuilder.asc(root.get(sortOption.getValue())));
		}

		if(!orderList.isEmpty())
			criteriaQuery.orderBy(orderList);

		TypedQuery<E> typedQuery = entityManager.createQuery(criteriaQuery);

		if(limit == 0)
			return typedQuery.getResultList();

		return typedQuery.setFirstResult((page - 1) * limit).setMaxResults(limit).getResultList();
	}

	private List<String> getSortFields(List<SortOption> sortOptionList) {
		List<String> sortFields = new ArrayList<>();

		for(SortOption sortOption : sortOptionList)
			sortFields.add(sortOption.getValue());

		return sortFields;
	}
}
```

Jak widać mamy tutaj już szereg gotowych metod :), których nie trzeba samemu ogarniać. I implementacja naszego repozytorium przedstawiona poniżej. Jak widać dziedziczymy po klasie AbstractRepository, a jako parametry generyczne podajemy klasę encji oraz klasę klucza tej encji. W naszym przypadku encja `User` ma klucz typu `Long`. Do tego oczywiście implementujemy interfejs UserRepository, żeby można było zrobić inną implementację tego interfejsu i używać ją np. do testów.

Warto wrócić teraz do adnotacji `@NamedQuery`, którą wcześniej spotkaliśmy przy klasie `User` - wykorzystujemy ją teraz przy implementacji metody `getByLogin`. Adnotacja ta zawierała po prostu zapytanie do bazy w języku JPQL (taki SQL operujący na obiektach), które teraz wykorzystujemy do pobrania użytkownika. Jeśli chodzi o Optional'a to nie będę wdawał się w szczegóły - można sobie poczytać, ale chodzi o to, żeby nie zwracać nulla tylko opakowac wynik w klasę która może być pusta. Adnotacja `@RequestScoped` nad klasą oznacza natomiast że jest to ziarno CDI, które za pomocą adnotacji `@Inject` będziemy mogli potem automatycznie wstrzyknąć do serwisu.

```java
@RequestScoped
public class UserRepositoryImpl extends AbstractRepository<User, Long> implements UserRepository{

	public UserRepositoryImpl() {
		super(User.class);
	}

	@Override
	public Optional<User> getByLogin(String login) {
		TypedQuery<User> getUserByLogin = createNamedQuery("User.getByLogin");
		getUserByLogin.setParameter("login", login);

		try {
			return Optional.of(getUserByLogin.getSingleResult());
		}
		catch(NoResultException e) {
			return Optional.empty();
		}
	}

	@Override
	public int deleteAll() {
		List<User> users = getAll();
		for(User user : users)
			deleteManaged(user);
		return users.size();
	}
}
```

Następnym krokiem jest stworzenie odpowiedniego serwisu, który również powinien bazować na interfejsie. Tworzymy więc interfejs serwisu i implementujemy go odpowiednimi metodami. Serwis korzysta z potrzebnych mu repozytoriów. Na diagramie klasy serwisu należy również oznaczać stereotypem `<<control>>`.

Przykładowy interfejs serwisu może wyglądać następująco:

```java
public interface UserService {

	User getUserById(Long id);
	User getUserByLogin(String login);
	List<User> getAllUsers();
	List<User> getAllUsers(String RSQLQuery, int page, int limit, List<SortOption> sortOptionList);
}
```
Powinien zawierać oczywiście więcej metod ale to tylko przykład.

A teraz czas na jego implementację:
```java
public class UserServiceImpl implements UserService {

	@Inject
	private UserRepository userRepository;

	public UserServiceImpl() {
		//default public constructor
	}

	//example method, exception should be more specified
	public User getUserById(Long id) {
		return userRepository.getById(id).orElseThrow(() -> new ApplicationException("404"));
	}

	//example method, exception should be more specified
	public User getUserByLogin(String login) {
		return userRepository.getByLogin(login).orElseThrow(() -> new ApplicationException("404"));
	}

	public List<User> getAllUsers() {
		return userRepository.getAll();
	}

	public List<User> getAllUsers(String RSQLQuery, int page, int limit, List<SortOption> sortOptionList) {
		return userRepository.getAll(RSQLQuery, page, limit, sortOptionList);
	}
}
```

Mamy już więc encje, mamy repozytoria i serwisy czyli elementy ``<<control>>`` czas więc na ``<<boundary>>``, czyli coś z czego korzysta nasz front! Klasy te nazywać będziemy endpointami i umieszczać w projekcie w podkatalogu web/rest/am lub web/rest/cm w zależności czy funkcjonalność dotyczy frontu administratora czy klienta. Zróbmy więc przykładowy endpoint dla administratora, który umożliwi pobieranie danych o użytkownikach.

```java
@RequestScoped
@Path("/am/users")
public class UserEndpoint {

	@Context
	private SecurityContext securityContext;

	@Inject
	private EndpointHelper requestHelper;

	@Inject
	private ResponseMaker responseMaker;

	@Inject
	private UserService userService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllUsers(
			@DefaultValue("") @QueryParam("search") String query,
			@DefaultValue("1") @QueryParam("page") int page,
			@DefaultValue("0") @QueryParam("limit") int limit,
			@QueryParam("sort") List<String> sortParams)
			throws ModelMapperException {
		List<SortOption> sortOptionList = requestHelper.parseSortParams(sortParams);
		List<User> users = userService.getAllUsers(query, page, limit, sortOptionList);
		List<UserDto> userDtos = ModelMapper.mapEntityCollectionToDtoList(users, UserDto.class);
		//password should be hidden?
		return responseMaker.successfulOperation(userDtos);
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@PathParam("id") long id) throws ModelMapperException {
		User user = userService.getUserById(id);
		UserDto userDto = ModelMapper.mapEntityToDto(user, UserDto.class);
		//password should be hidden?
		return responseMaker.successfulOperation(userDto);
	}
}
```

Powyżej pojawia się pełno nowych rzeczy, niestety. ``@RequestScoped`` na razie zostawiamy w spokoju - endpoint będzie ziarniem CDI i nas to nie interesuje.
``@Path`` informuje nas o ścieżce dostępu do endpointu. Nasze api ma adres ``localhost:8080/api`` a więc ten endpoint będzie dostępny pod adresem ``localhost:8080/api/am/users``. To ``am`` bo moduł administracyjny :).
Dalej mamy pełno pół, które do momentu implementacji są nieistotne - są one tylko pomocnicze i bez nich też się da to samo zrobić. Nas interesuje tylko wstrzyknięcie `UserService` z którego będziemy korzystali. Poniżej znajdują się dwie metody HTTP GET służące do pobieranie danych w formacie JSON. Nie będę omawiał jak działa REST - to znajdziecie sobie [tutaj](http://www.samouczekprogramisty.pl/rest-web-service-z-java-ee-czesc-1/) i [tutaj](http://www.samouczekprogramisty.pl/rest-web-service-z-java-ee-czesc-2). Jak widać ta metoda niżej zwraca użytkownika po id i tu nie ma żadnej filozofii. Ta metoda wyżej zwraca natomiast użytkowników w zależnośc od parametrów zapytania użytkownika. Ale jak to działa - a to jest właśnie ten RSQL. Użytkownik może wejść pod adres ``localhost:8080/api/am/users`` i dostanie użytkowników. Może wejść pod adres ``localhost:8080/api/am/users/{id}`` i dostanie użytkownika o id ``{id}``. Ale co gdyby chciał filtrować wynik w sposób bardziej złożony? Pisanie wszystkich możliwych metod zajęło by zbyt wiele czasu, więc piszemy ręcznie tylko takie które będą mega często wykorzystywane np. ``getUserByLogin``. A do innych cełów nasz frontend dev ma RSQL.

Użycie RSQL w praktyce:

- pobierz użytkowników o id > 2:
``localhost:8080/api/am/users?search=id>2``.
- pobierz użytkowników których nick zaczyna się od `C`: ``localhost:8080/api/am/users?search=login==C*
- a do tego posortuj po id malejąco: ``localhost:8080/api/am/users?search={query}&sort=>id

Jak widać nic trudnego - ta metoda, którą można kopiować do endpointów gdzie to jest potrzebne sama za nas to wszystko zrobi. Mamy do dyspozycji operatory takie jak ``,``, czyli OR ``;``, czyli AND i inne opisane [tutaj](https://github.com/tennaito/rsql-jpa).

Dokładne oko wychwyci jeszcze coś - mianowicie zawsze zwracamy JSON'a z jakiegoś UsetDto a nie User. Jest to tak zwany ``Data transfer object``. Można oczywiście sotoswać podejście "encja na twarz i pchasz" ale przy większym projekcie zgranie modelu biznesowego z tym co przekazujemy do widoku jest bardzo niewygodne. Najbardziej przydaje się to jednak gdy występuje pełno powiązań klas. Załóżmy, że mamy klasę ``Order``, która ma pole typu ``User`` o nazwie user i trzyma tam referencję do właściciela zamówienia. Przy pobieraniu zamówienia nasz JSON zawierałby też pełną informację o użytkowniku i teoretycznie nie stanowiłoby to żadnego problemu. Co jednak gdyby klasa ``User`` zawierała w sobie inne referencje, a sam ``Order`` miał kilka takich pól? Przekazywanie takiego serializowanego grafu to jeszcze pół biedy, ale gdybyśmy chcieli wykonać metodę POST do dodania nowego obiektu klasy ``Oder`` należałoby również zawrzeć tam cały ten graf obiektów... Można to rozwiązać na kilka sposóbów np. poprzez referencję w JSON'ie (z tego rozwiązania korzysta Spring). My jednak wykorzystamy ``Dto``, które do takiego projektu nadaje się świetnie. Może się też tak zdarzyć, że będziemy chcieli przekazywać jakiś większy graf obiektów, ale z tym też nie ma problemu, bo `Dto` może zawierać w sobie inne ``Dto``, a ponad to ma też szereg innych udogodnień :). Na przykład zmiast przekazywać obiekt ``Order`` z polem ``user`` przekażemy go z polem ``userId``,  albo skonwertujemy niewygodną w serializacji datę (bo wtedy obiekt daty zawiera informację o kalendarzu i innych śmieciach) do stringa.

Przykładowe ``Dto`` dla klasy ``User`` znajduje się poniżej. Są tutaj użyte dwa typy adnotacji - adnotacje od serializatora Javy do JSON'a i adnotacje od ModelMapper'a (mojego autorstwa na potrzeby projektu), który zmapuje nam klasę ``User`` na klasę ``UserDto``. Do dyspozycji mamy adnotacje takie jak:

-	@MmValue - po prostu kopiuje wartość
-	@MmNestedValue - kopiuje wartość zagnieżdzoną np. id albo login użytkownika zamiast całego pola user
-	@MmConvertedValue - kopiuje wartość skonwertowaną np. datę, można napisać swój konwerter jeśli potrzebujemy czegoś dodatkowego
-	@MmMethodValue - kopiuje wartość zwróconą przez jakąś metodę z encji
-	@MmDto - kopiuje inne Dto  np. Order zamiast User zawiera UserDto

```java
@JsonbNillable
@JsonbPropertyOrder({"id", "login", "password"})
public class UserDto implements Serializable {

	@MmValue(fieldName = "id")
	private Long id;

	@MmValue(fieldName = "login")
	private String login;

	@MmValue(fieldName = "password")
	private String password;

	@MmConvertedValue(fieldName = "createdAt", convertertClass = LocalDateTimeConverter.class)
	private String createdAt;

	public UserDto() {
		//default public constructor
	}

	//getters and setters omitted for brevity
}
```

Gdy cofniecie się do przykładu z Endpoint'em możecie zauważyć wywołanie metody do mapowania ``User`` na ``UserDto``. To od waszego projektu zależy co ``Dto`` danej encji będzie zawierał, ale polecam to dobrze przemyśleć, szczególnie zanim wpakujecie tam całe inne `Dto`. I jeszcze jedno, nawet jeśli zwracamy dokładnie to samo co zawiera encja to i tak robimy ``Dto``, żeby uniknąć sytuacji, że raz zwracamy encję a raz nie...

I na koniec przykładowy diagram klas dla tego co zostało tutaj pokazane. Zostało to wygenerowane za pomocą programu Astah i poprawione oczywiście bo nie zawsze wszystko się tak ładnie samo wygeneruje. A tutaj [link](https://i.imgur.com/ltOFQrs.png) dla lepszej widoczności. Dobrze by było gdyby wszyscy robili w jednym, choć to tylko propozycja. Astash jest dostępny dla studentów za darmo do pobrania na ich stronie lub też można skorzystać z 30 dniowej wersji testowej. [Astah](http://astah.net/)

Ewentualnie gdyby ktoś chciał link do pliku asta z tego to [proszę](https://drive.google.com/open?id=12TWH8zlmD5flkInYyYR_TXWLa4nXpHHz). Ale należy sprawdzić czy te podstawowe klasy na pewno mają wszystko co powinno i niczego nie ucięło. To tylko przykład i nie daję głowy, że te relacje są dobre :D. Ja męczyłem się ze swoimi i nie będę tego dokładnie analizował.

![Alt text](https://i.imgur.com/ltOFQrs.png)


## Oprogramowanie
Do tworzenia projektu wykorzystujemy środowisko InteliJ IDEA do którego każdy student może otrzymać darmową licencję. [Pobierz środowisko.](https://www.jetbrains.com/student/)

Ponadto potrzebny będzie serwer aplikacyjny JEE (darmowy Wildfly) oraz System zarządzania bazą danych PostgreSQL. Oba te serwery znajdują się udostępnione już skonfigurowane i gotowe do uruchomienia pod tym [linkiem](https://drive.google.com/open?id=1D2ZmJdpTj4VO__oftmDHjkFreTMUrD91).

Po rozpakowaniu paczki powinniśmy mieć dwa foldery `IO_Postgresql` oraz `IO_Wildfly`. Najlepiej skopiować je do jakiegoś wspólnego katalogu np `D:\Server`. Następnie w katalogu `IO_Postgresql` należy edytować plik `settings.xml` i ustawić tam ścieżki (tutaj chyba każdy sobie poradzi po otworzeniu pliku). Teraz serwer bazy danych powinien być możliwy do uruchomienia za pomocą dołączonego programu `PostgresqlManager.exe` - teraz uruchamiamy go wpisując tam polecenie `start`. Należy teraz pobrać na swój komputer kod z repozytorium i otworzyć go w środowisku InteliJ. Odpalamy więc IDE, wybieramy Open i wskazujemy folder z projektem. A następnie postepujemy tak jak na SS'ach.

![alt text](https://image.prntscr.com/image/3QZR6VzJTF_nVIfWk9YE0A.png)
![alt text](https://image.prntscr.com/image/8iGmgjp4RH6zEUqM8CPlfQ.png)
![alt text](https://image.prntscr.com/image/ZCLCiPU2R5itsCOpj-Nc-w.png)
![alt text](https://image.prntscr.com/image/s_g51JgaQmeQ6GT7MNsrwQ.png)
![alt text](https://image.prntscr.com/image/b91hrmCbSlyGrSOSoKJT8w.png)
![alt text](https://image.prntscr.com/image/583Bf68EQW2-SewDONnBjw.png)
![alt text](https://image.prntscr.com/image/zHhCysVXTrqOqWvChc-Rxg.png)

No i zaczynamy kodować :).
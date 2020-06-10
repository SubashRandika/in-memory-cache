package com.wiley.codechallenge.inmemorycache;

import com.wiley.codechallenge.inmemorycache.entity.Person;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InMemoryCacheApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	private String serverHost;

	@BeforeEach
	public void init() {
		serverHost = "http://localhost:" + port;
	}

	@Test
	@Order(1)
	void checkLRUCacheIsEmpty() {
		String responseSize = restTemplate.getForObject(serverHost + "/api/cache/size", String.class);
		assertTrue(responseSize.contains("\"size\":0"));
	}

	@Test
	@Order(2)
	void addThreePersonObjectsToLRUCache() {
		Person lina = new Person("Lina", 24, "America");
		String responseLina = restTemplate.postForObject(serverHost + "/api/cache/1", lina, String.class);
		assertTrue(responseLina.contains("\"code\":200"));

		Person yuan = new Person("Yuan", 29, "China");
		String responseYuan = restTemplate.postForObject(serverHost + "/api/cache/2", yuan, String.class);
		assertTrue(responseYuan.contains("\"code\":200"));

		Person rahul = new Person("Rahul", 32, "India");
		String responseRahul = restTemplate.postForObject(serverHost + "/api/cache/3", rahul, String.class);
		assertTrue(responseRahul.contains("\"code\":200"));

		String responseSize = restTemplate.getForObject(serverHost + "/api/cache/size", String.class);
		assertTrue(responseSize.contains("\"size\":3"));
	}

	@Test
	@Order(3)
	void accessLinaAndRahulPersonObjectsFromLRUCache() {
		String linaObjectResponse = restTemplate.getForObject(serverHost + "/api/cache/1", String.class);
		assertTrue(linaObjectResponse.contains("\"name\":\"Lina\""));

		String rahulObjectResponse = restTemplate.getForObject(serverHost + "/api/cache/3", String.class);
		assertTrue(rahulObjectResponse.contains("\"name\":\"Rahul\""));

		String responseSize = restTemplate.getForObject(serverHost + "/api/cache/size", String.class);
		assertTrue(responseSize.contains("\"size\":3"));
	}

	@Test
	@Order(4)
	void addAnotherThreePersonObjectsToLRUCache() {
		Person david = new Person("David", 28, "England");
		String responseDavid = restTemplate.postForObject(serverHost + "/api/cache/4", david, String.class);
		assertTrue(responseDavid.contains("\"code\":200"));

		Person tania = new Person("Tania", 19, "Sri Lanka");
		String responseTania = restTemplate.postForObject(serverHost + "/api/cache/5", tania, String.class);
		assertTrue(responseTania.contains("\"code\":200"));

		Person scott = new Person("Scott", 35, "Australia");
		String responseScott = restTemplate.postForObject(serverHost + "/api/cache/6", scott, String.class);
		assertTrue(responseScott.contains("\"code\":200"));

		String responseSize = restTemplate.getForObject(serverHost + "/api/cache/size", String.class);
		assertTrue(responseSize.contains("\"size\":5"));
	}

	@Test
	@Order(5)
	void checkPersonYuanIsInTheLRUCache() {
		// Cache is full now (size = 5)
		// According to LRU policy Yuan is least recently used person object in the cache.
		// So Yuan(key is 2) should not in the cache and already evicted.

		String allObjectsResponse = restTemplate.getForObject(serverHost + "/api/cache/check", String.class);
		assertFalse(allObjectsResponse.contains("\"2\":{\"name\":\"Lina\""));
	}

	@Test
	@Order(6)
	void accessPersonDavidObjectFromLRUCache() {
		String responseDavid = restTemplate.getForObject(serverHost + "/api/cache/4", String.class);
		assertTrue(responseDavid.contains("\"name\":\"David\""));

		String responseSize = restTemplate.getForObject(serverHost + "/api/cache/size", String.class);
		assertTrue(responseSize.contains("\"size\":5"));
	}

	@Test
	@Order(7)
	void addNovaPersonObjectIntoLRUCache() {
		Person nova = new Person("Nova", 28, "Russia");
		String responseNova = restTemplate.postForObject(serverHost + "/api/cache/7", nova, String.class);
		assertTrue(responseNova.contains("\"code\":200"));

		// Cache is full now (size = 5)
		// According to LRU policy Lina is least recently used person object in the cache.
		// So Lina(key is 1) should not in the cache and already evicted.
		String allObjectsResponse = restTemplate.getForObject(serverHost + "/api/cache/check", String.class);
		assertFalse(allObjectsResponse.contains("\"1\":{\"name\":\"Lina\""));

		String responseSize = restTemplate.getForObject(serverHost + "/api/cache/size", String.class);
		assertTrue(responseSize.contains("\"size\":5"));
	}

	@Test
	@Order(8)
	void addLewisPersonObjectIntoLRUCache() {
		Person lewis = new Person("Lewis", 38, "Jamaica");
		String responseLewis = restTemplate.postForObject(serverHost + "/api/cache/8", lewis, String.class);
		assertTrue(responseLewis.contains("\"code\":200"));

		// Cache is full now (size = 5)
		// According to LRU policy Rahul is least recently used person object in the cache.
		// So Rahul(key is 3) should not in the cache and already evicted.
		String allObjectsResponse = restTemplate.getForObject(serverHost + "/api/cache/check", String.class);
		assertFalse(allObjectsResponse.contains("\"3\":{\"name\":\"Rahul\""));

		String responseSize = restTemplate.getForObject(serverHost + "/api/cache/size", String.class);
		assertTrue(responseSize.contains("\"size\":5"));
	}

	@Test
	@Order(9)
	void getNotContainingObjectFromLRUCache() {
		String responseNotExistObject = restTemplate.getForObject(serverHost + "/api/cache/10", String.class);
		assertTrue(responseNotExistObject.contains("\"code\":404"));
	}

	@Test
	@Order(10)
	void updateExistingPersonObjectInLRUCache() {
		Person abdul = new Person("Abdul", 48, "Pakistan");
		String responseAbdul = restTemplate.postForObject(serverHost + "/api/cache/8", abdul, String.class);
		assertTrue(responseAbdul.contains("\"code\":200"));

		String allObjectsResponse = restTemplate.getForObject(serverHost + "/api/cache/check", String.class);
		assertTrue(allObjectsResponse.contains("\"8\":{\"name\":\"Abdul\""));

		String responseSize = restTemplate.getForObject(serverHost + "/api/cache/size", String.class);
		assertTrue(responseSize.contains("\"size\":5"));
	}

	@Test
	@Order(11)
	void clearEntireLRUCache() {
		String cacheClearResponse = restTemplate.getForObject(serverHost + "/api/cache/clear", String.class);
		assertTrue(cacheClearResponse.contains("\"code\":200"));

		String responseSize = restTemplate.getForObject(serverHost + "/api/cache/size", String.class);
		assertTrue(responseSize.contains("\"size\":0"));
	}

}

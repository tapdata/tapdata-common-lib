package io.tapdata.modules.api.net.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class NodeRegistryTest {

	@Test
	void idShouldKeepCompatibleWhenInstanceIdMissing() {
		NodeRegistry first = new NodeRegistry()
				.ips(Arrays.asList("127.0.0.1", "192.168.0.10"))
				.httpPort(3000)
				.wsPort(8246)
				.type("proxy");
		NodeRegistry second = new NodeRegistry()
				.ips(Arrays.asList("127.0.0.1", "192.168.0.10"))
				.httpPort(3000)
				.wsPort(8246)
				.type("proxy");

		Assertions.assertEquals(first.id(), second.id());
	}

	@Test
	void idShouldUseInstanceIdWhenPresent() {
		NodeRegistry first = new NodeRegistry()
				.instanceId("tm-0")
				.ips(Arrays.asList("127.0.0.1"))
				.httpPort(3000)
				.wsPort(8246)
				.type("proxy");
		NodeRegistry second = new NodeRegistry()
				.instanceId("tm-1")
				.ips(Arrays.asList("127.0.0.1"))
				.httpPort(3000)
				.wsPort(8246)
				.type("proxy");

		Assertions.assertNotEquals(first.id(), second.id());
	}

	@Test
	void idShouldStayStableWhenInstanceAddressChanges() {
		NodeRegistry nodeRegistry = new NodeRegistry()
				.instanceId("tm-0")
				.ips(Arrays.asList("127.0.0.1"))
				.httpPort(3000)
				.wsPort(8246)
				.type("proxy");
		String id = nodeRegistry.id();

		nodeRegistry.ips(Arrays.asList("192.168.0.10")).httpPort(3001).wsPort(8247);

		Assertions.assertEquals(id, nodeRegistry.id());
	}
}

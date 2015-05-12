package org.squiddev.cctweaks.core.network;

import codechicken.lib.vec.BlockCoord;
import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.squiddev.cctweaks.api.network.ISearchLoc;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.core.network.mock.BasicNetwork;
import org.squiddev.cctweaks.core.network.mock.KeyedNetworkNode;
import org.squiddev.cctweaks.core.network.mock.TestData;

import java.io.InputStreamReader;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests various facts about the network visitor
 */
@RunWith(Parameterized.class)
public class NetworkVisitorTest {
	private final BasicNetwork network;

	public NetworkVisitorTest(String name, BasicNetwork network) {
		this.network = network;
	}

	@Parameterized.Parameters(name = "{0}")
	public static Object[][] data() {
		TestData[] data = new Gson().fromJson(
			new InputStreamReader(NetworkVisitorTest.class.getResourceAsStream("data.json")),
			TestData[].class
		);

		Object[][] result = new Object[data.length][];
		for (int i = 0; i < data.length; i++) {
			TestData item = data[i];
			result[i] = new Object[]{
				item.name,
				new BasicNetwork(item)
			};
		}

		return result;
	}

	@Test
	public void testCounts() {
		network.reset();

		for (ISearchLoc loc : NetworkAPI.visitor().visitNetwork(network, 0, 0, 0)) {
			loc.getNode().networkInvalidated();
		}

		for (Map.Entry<BlockCoord, KeyedNetworkNode> location : network) {
			Integer count = network.count.get(location.getValue().key);
			if (count != null) {
				assertEquals("Location " + location, count.intValue(), location.getValue().invalidated());
			}
		}
	}
}

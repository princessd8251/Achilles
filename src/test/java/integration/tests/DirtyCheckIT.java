package integration.tests;

import static fr.doan.achilles.columnFamily.ColumnFamilyHelper.normalizeCanonicalName;
import static fr.doan.achilles.common.CassandraDaoTest.getCluster;
import static fr.doan.achilles.common.CassandraDaoTest.getEntityDao;
import static fr.doan.achilles.common.CassandraDaoTest.getKeyspace;
import static fr.doan.achilles.serializer.Utils.LONG_SRZ;
import static org.fest.assertions.api.Assertions.assertThat;
import integration.tests.entity.CompleteBean;
import integration.tests.entity.CompleteBeanTestBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import me.prettyprint.hector.api.beans.AbstractComposite.ComponentEquality;
import me.prettyprint.hector.api.beans.DynamicComposite;

import org.apache.cassandra.utils.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.doan.achilles.dao.GenericEntityDao;
import fr.doan.achilles.entity.factory.ThriftEntityManagerFactoryImpl;
import fr.doan.achilles.entity.manager.ThriftEntityManager;
import fr.doan.achilles.entity.metadata.PropertyType;
import fr.doan.achilles.holder.KeyValueHolder;

/**
 * ThriftEntityManagerDirtyCheckIT
 * 
 * @author DuyHai DOAN
 * 
 */
public class DirtyCheckIT
{
	private final String ENTITY_PACKAGE = "integration.tests.entity";
	private GenericEntityDao<Long> dao = getEntityDao(LONG_SRZ,
			normalizeCanonicalName(CompleteBean.class.getCanonicalName()));

	private ThriftEntityManagerFactoryImpl factory = new ThriftEntityManagerFactoryImpl(
			getCluster(), getKeyspace(), ENTITY_PACKAGE, true);

	private ThriftEntityManager em = (ThriftEntityManager) factory.createEntityManager();

	private CompleteBean bean;

	@Before
	public void setUp()
	{
		bean = CompleteBeanTestBuilder.builder().randomId().name("DuyHai").age(35L)
				.addFriends("foo", "bar").addFollowers("George", "Paul").addPreference(1, "FR")
				.addPreference(2, "Paris").addPreference(3, "75014").buid();

		bean = em.merge(bean);
	}

	@Test
	public void should_dirty_check_list_element_add() throws Exception
	{
		bean.getFriends().add("qux");

		em.merge(bean);

		DynamicComposite startComp = startCompForList();
		DynamicComposite endComp = endComptForList();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(3);
		assertThat(columns.get(2).right).isEqualTo("qux");
	}

	@Test
	public void should_dirty_check_list_element_add_at_index() throws Exception
	{
		bean.getFriends().add(1, "qux");

		em.merge(bean);

		DynamicComposite startComp = startCompForList();
		DynamicComposite endComp = endComptForList();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(3);
		assertThat(columns.get(1).right).isEqualTo("qux");
		assertThat(columns.get(2).right).isEqualTo("bar");
	}

	@Test
	public void should_dirty_check_list_element_add_all() throws Exception
	{
		bean.getFriends().addAll(Arrays.asList("qux", "baz"));

		em.merge(bean);

		DynamicComposite startComp = startCompForList();
		DynamicComposite endComp = endComptForList();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(4);
		assertThat(columns.get(2).right).isEqualTo("qux");
		assertThat(columns.get(3).right).isEqualTo("baz");
	}

	@Test
	public void should_dirty_check_list_element_clear() throws Exception
	{
		bean.getFriends().clear();

		em.merge(bean);

		DynamicComposite startComp = startCompForList();
		DynamicComposite endComp = endComptForList();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(0);
	}

	@Test
	public void should_dirty_check_list_element_remove_at_index() throws Exception
	{
		bean.getFriends().remove(0);

		em.merge(bean);

		DynamicComposite startComp = startCompForList();
		DynamicComposite endComp = endComptForList();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(1);
		assertThat(columns.get(0).right).isEqualTo("bar");
	}

	@Test
	public void should_dirty_check_list_element_remove_element() throws Exception
	{
		bean.getFriends().remove("bar");

		em.merge(bean);

		DynamicComposite startComp = startCompForList();
		DynamicComposite endComp = endComptForList();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(1);
		assertThat(columns.get(0).right).isEqualTo("foo");
	}

	@Test
	public void should_dirty_check_list_element_remove_all() throws Exception
	{
		bean.getFriends().removeAll(Arrays.asList("foo", "qux"));

		em.merge(bean);

		DynamicComposite startComp = startCompForList();
		DynamicComposite endComp = endComptForList();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(1);
		assertThat(columns.get(0).right).isEqualTo("bar");
	}

	@Test
	public void should_dirty_check_list_element_retain_all() throws Exception
	{
		bean.getFriends().retainAll(Arrays.asList("foo", "qux"));

		em.merge(bean);

		DynamicComposite startComp = startCompForList();
		DynamicComposite endComp = endComptForList();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(1);
		assertThat(columns.get(0).right).isEqualTo("foo");
	}

	@Test
	public void should_dirty_check_list_element_sub_list_remove() throws Exception
	{
		bean.getFriends().subList(0, 1).remove(0);

		em.merge(bean);

		DynamicComposite startComp = startCompForList();
		DynamicComposite endComp = endComptForList();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(1);
		assertThat(columns.get(0).right).isEqualTo("bar");
	}

	@Test
	public void should_dirty_check_list_element_set() throws Exception
	{
		bean.getFriends().set(1, "qux");

		em.merge(bean);

		DynamicComposite startComp = startCompForList();
		DynamicComposite endComp = endComptForList();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(2);
		assertThat(columns.get(1).right).isEqualTo("qux");
	}

	@Test
	public void should_dirty_check_list_element_iterator_remove() throws Exception
	{
		Iterator<String> iter = bean.getFriends().iterator();

		iter.next();
		iter.remove();

		em.merge(bean);

		DynamicComposite startComp = startCompForList();
		DynamicComposite endComp = endComptForList();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(1);
		assertThat(columns.get(0).right).isEqualTo("bar");
	}

	@Test
	public void should_dirty_check_list_element_list_iterator_remove() throws Exception
	{
		Iterator<String> iter = bean.getFriends().listIterator();

		iter.next();
		iter.remove();

		em.merge(bean);

		DynamicComposite startComp = startCompForList();
		DynamicComposite endComp = endComptForList();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(1);
		assertThat(columns.get(0).right).isEqualTo("bar");
	}

	@Test
	public void should_dirty_check_list_element_list_iterator_set() throws Exception
	{
		ListIterator<String> iter = bean.getFriends().listIterator();

		iter.next();
		iter.set("qux");

		em.merge(bean);

		DynamicComposite startComp = startCompForList();
		DynamicComposite endComp = endComptForList();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(2);
		assertThat(columns.get(0).right).isEqualTo("qux");
	}

	@Test
	public void should_dirty_check_map_put_element() throws Exception
	{
		bean.getPreferences().put(4, "test");

		em.merge(bean);

		DynamicComposite startComp = startCompForMap();
		DynamicComposite endComp = endCompForMap();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(4);
		assertThat(((KeyValueHolder) columns.get(3).right).getValue()).isEqualTo("test");
	}

	@Test
	public void should_dirty_check_map_remove_key() throws Exception
	{
		bean.getPreferences().remove(1);

		em.merge(bean);

		DynamicComposite startComp = startCompForMap();
		DynamicComposite endComp = endCompForMap();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(2);
		assertThat(((KeyValueHolder) columns.get(0).right).getValue()).isEqualTo("Paris");
		assertThat(((KeyValueHolder) columns.get(1).right).getValue()).isEqualTo("75014");
	}

	@Test
	public void should_dirty_check_map_put_all() throws Exception
	{
		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(3, "75015");
		map.put(4, "test");
		bean.getPreferences().putAll(map);

		em.merge(bean);

		DynamicComposite startComp = startCompForMap();
		DynamicComposite endComp = endCompForMap();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(4);
		assertThat(((KeyValueHolder) columns.get(2).right).getValue()).isEqualTo("75015");
		assertThat(((KeyValueHolder) columns.get(3).right).getValue()).isEqualTo("test");
	}

	@Test
	public void should_dirty_check_map_keyset_remove() throws Exception
	{
		bean.getPreferences().keySet().remove(1);

		em.merge(bean);

		DynamicComposite startComp = startCompForMap();
		DynamicComposite endComp = endCompForMap();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(2);
		assertThat(((KeyValueHolder) columns.get(0).right).getValue()).isEqualTo("Paris");
		assertThat(((KeyValueHolder) columns.get(1).right).getValue()).isEqualTo("75014");
	}

	@Test
	public void should_dirty_check_map_keyset_remove_all() throws Exception
	{
		bean.getPreferences().keySet().removeAll(Arrays.asList(1, 2, 5));

		em.merge(bean);

		DynamicComposite startComp = startCompForMap();
		DynamicComposite endComp = endCompForMap();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(1);
		assertThat(((KeyValueHolder) columns.get(0).right).getValue()).isEqualTo("75014");
	}

	@Test
	public void should_dirty_check_map_keyset_retain_all() throws Exception
	{
		bean.getPreferences().keySet().retainAll(Arrays.asList(1, 3));

		em.merge(bean);

		DynamicComposite startComp = startCompForMap();
		DynamicComposite endComp = endCompForMap();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(2);
		assertThat(((KeyValueHolder) columns.get(0).right).getValue()).isEqualTo("FR");
		assertThat(((KeyValueHolder) columns.get(1).right).getValue()).isEqualTo("75014");
	}

	@Test
	public void should_dirty_check_map_keyset_iterator_remove() throws Exception
	{
		Iterator<Integer> iter = bean.getPreferences().keySet().iterator();

		iter.next();
		iter.remove();

		em.merge(bean);

		DynamicComposite startComp = startCompForMap();
		DynamicComposite endComp = endCompForMap();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(2);
		assertThat(((KeyValueHolder) columns.get(0).right).getValue()).isEqualTo("Paris");
		assertThat(((KeyValueHolder) columns.get(1).right).getValue()).isEqualTo("75014");
	}

	@Test
	public void should_dirty_check_map_valueset_remove() throws Exception
	{
		bean.getPreferences().values().remove("FR");

		em.merge(bean);

		DynamicComposite startComp = startCompForMap();
		DynamicComposite endComp = endCompForMap();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(2);
		assertThat(((KeyValueHolder) columns.get(0).right).getValue()).isEqualTo("Paris");
		assertThat(((KeyValueHolder) columns.get(1).right).getValue()).isEqualTo("75014");
	}

	@Test
	public void should_dirty_check_map_valueset_remove_all() throws Exception
	{
		bean.getPreferences().values().removeAll(Arrays.asList("FR", "Paris", "test"));

		em.merge(bean);

		DynamicComposite startComp = startCompForMap();

		DynamicComposite endComp = endCompForMap();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(1);
		assertThat(((KeyValueHolder) columns.get(0).right).getValue()).isEqualTo("75014");
	}

	@Test
	public void should_dirty_check_map_valueset_retain_all() throws Exception
	{
		bean.getPreferences().values().retainAll(Arrays.asList("FR", "Paris", "test"));

		em.merge(bean);

		DynamicComposite startComp = startCompForMap();
		DynamicComposite endComp = endCompForMap();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(2);
		assertThat(((KeyValueHolder) columns.get(0).right).getValue()).isEqualTo("FR");
		assertThat(((KeyValueHolder) columns.get(1).right).getValue()).isEqualTo("Paris");
	}

	@Test
	public void should_dirty_check_map_valueset_iterator_remove() throws Exception
	{
		Iterator<String> iter = bean.getPreferences().values().iterator();

		iter.next();
		iter.remove();

		em.merge(bean);

		DynamicComposite startComp = startCompForMap();
		DynamicComposite endComp = endCompForMap();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(2);
		assertThat(((KeyValueHolder) columns.get(0).right).getValue()).isEqualTo("Paris");
		assertThat(((KeyValueHolder) columns.get(1).right).getValue()).isEqualTo("75014");
	}

	@Test
	public void should_dirty_check_map_entrySet_remove_entry() throws Exception
	{

		Set<Entry<Integer, String>> entrySet = bean.getPreferences().entrySet();

		Entry<Integer, String> entry = entrySet.iterator().next();

		entrySet.remove(entry);

		em.merge(bean);

		DynamicComposite startComp = startCompForMap();
		DynamicComposite endComp = endCompForMap();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(2);
		assertThat(((KeyValueHolder) columns.get(0).right).getValue()).isEqualTo("Paris");
		assertThat(((KeyValueHolder) columns.get(1).right).getValue()).isEqualTo("75014");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_dirty_check_map_entrySet_remove_all_entry() throws Exception
	{

		Set<Entry<Integer, String>> entrySet = bean.getPreferences().entrySet();

		Iterator<Entry<Integer, String>> iterator = entrySet.iterator();

		Entry<Integer, String> entry1 = iterator.next();
		Entry<Integer, String> entry2 = iterator.next();

		entrySet.removeAll(Arrays.asList(entry1, entry2));

		em.merge(bean);

		DynamicComposite startComp = startCompForMap();
		DynamicComposite endComp = endCompForMap();

		List<Pair<DynamicComposite, Object>> columns = dao.findColumnsRange(bean.getId(),
				startComp, endComp, false, 20);

		assertThat(columns).hasSize(1);
		assertThat(((KeyValueHolder) columns.get(0).right).getValue()).isEqualTo("75014");
	}

	private DynamicComposite endComptForList()
	{
		DynamicComposite endComp = new DynamicComposite();
		endComp.addComponent(0, PropertyType.LAZY_LIST.flag(), ComponentEquality.EQUAL);
		endComp.addComponent(1, "friends", ComponentEquality.EQUAL);
		endComp.addComponent(2, 5, ComponentEquality.GREATER_THAN_EQUAL);
		return endComp;
	}

	private DynamicComposite startCompForList()
	{
		DynamicComposite startComp = new DynamicComposite();
		startComp.addComponent(0, PropertyType.LAZY_LIST.flag(), ComponentEquality.EQUAL);
		startComp.addComponent(1, "friends", ComponentEquality.EQUAL);
		startComp.addComponent(2, 0, ComponentEquality.EQUAL);
		return startComp;
	}

	private DynamicComposite endCompForMap()
	{
		DynamicComposite endComp = new DynamicComposite();
		endComp.addComponent(0, PropertyType.MAP.flag(), ComponentEquality.EQUAL);
		endComp.addComponent(1, "preferences", ComponentEquality.EQUAL);
		endComp.addComponent(2, 5, ComponentEquality.GREATER_THAN_EQUAL);
		return endComp;
	}

	private DynamicComposite startCompForMap()
	{
		DynamicComposite startComp = new DynamicComposite();
		startComp.addComponent(0, PropertyType.MAP.flag(), ComponentEquality.EQUAL);
		startComp.addComponent(1, "preferences", ComponentEquality.EQUAL);
		startComp.addComponent(2, 0, ComponentEquality.EQUAL);
		return startComp;
	}

	@After
	public void tearDown()
	{
		dao.truncate();
	}
}

/*
 * Copyright 2015 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.internal;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.Sort;
import io.realm.TestHelper;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class JNITableTest {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private Table t;

    @Before
    public void setUp() {
        t = new Table();
    }

    @Test
    public void tableToString() {
        Table t = new Table();

        t.addColumn(RealmFieldType.STRING, "stringCol");
        t.addColumn(RealmFieldType.INTEGER, "intCol");
        t.addColumn(RealmFieldType.BOOLEAN, "boolCol");

        t.add("s1", 1, true);
        t.add("s2", 2, false);

        String expected = "The Table contains 3 columns: stringCol, intCol, boolCol. And 2 rows.";
        assertEquals(expected, t.toString());
    }

    @Test
    public void rowOperationsOnZeroRow(){

        Table t = new Table();
        // Remove rows without columns
        try { t.remove(0);  fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException ignored) {}
        try { t.remove(10); fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException ignored) {}

        // Column added, remove rows again
        t.addColumn(RealmFieldType.STRING, "");
        try { t.remove(0);  fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException ignored) {}
        try { t.remove(10); fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException ignored) {}

    }

    @Test
    public void zeroColOperations() {
        Table tableZeroCols = new Table();

        // Add rows
        try { tableZeroCols.add("val");         fail("No columns in table"); } catch (IndexOutOfBoundsException ignored) {}
        try { tableZeroCols.addEmptyRow();      fail("No columns in table"); } catch (IndexOutOfBoundsException ignored) {}
        try { tableZeroCols.addEmptyRows(10);   fail("No columns in table"); } catch (IndexOutOfBoundsException ignored) {}


        // Col operations
        try { tableZeroCols.removeColumn(0);                fail("No columns in table"); } catch (ArrayIndexOutOfBoundsException ignored) {}
        try { tableZeroCols.renameColumn(0, "newName");     fail("No columns in table"); } catch (ArrayIndexOutOfBoundsException ignored) {}
        try { tableZeroCols.removeColumn(10);               fail("No columns in table"); } catch (ArrayIndexOutOfBoundsException ignored) {}
        try { tableZeroCols.renameColumn(10, "newName");    fail("No columns in table"); } catch (ArrayIndexOutOfBoundsException ignored) {}
    }

    @Test
    public void findFirstNonExisting() {
        Table t = TestHelper.getTableWithAllColumnTypes();
        t.add(new byte[]{1, 2, 3}, true, new Date(1384423149761L), 4.5d, 5.7f, 100, "string");

        assertEquals(-1, t.findFirstBoolean(1, false));
        // FIXME: reenable when core implements find_first_timestamp(): assertEquals(-1, t.findFirstDate(2, new Date(138442314986l)));
        assertEquals(-1, t.findFirstDouble(3, 1.0d));
        assertEquals(-1, t.findFirstFloat(4, 1.0f));
        assertEquals(-1, t.findFirstLong(5, 50));
    }

    @Test
    public void findFirst() {
        final int TEST_SIZE = 10;
        Table t = TestHelper.getTableWithAllColumnTypes();
        for (int i = 0; i < TEST_SIZE; i++) {
            t.add(new byte[]{1,2,3}, true, new Date(i), (double)i, (float)i, i, "string " + i);
        }
        t.add(new byte[]{1, 2, 3}, true, new Date(TEST_SIZE), (double) TEST_SIZE, (float) TEST_SIZE, TEST_SIZE, "");

        assertEquals(0, t.findFirstBoolean(1, true));
        for (int i = 0; i < TEST_SIZE; i++) {
            assertEquals(i, t.findFirstDate(2, new Date(i)));
            assertEquals(i, t.findFirstDouble(3, (double) i));
            assertEquals(i, t.findFirstFloat(4, (float) i));
            assertEquals(i, t.findFirstLong(5, i));
        }

        try {
            t.findFirstString(6, null);
            fail();
        } catch (IllegalArgumentException ignored) {}

        try {
            t.findFirstDate(2, null);
            fail();
        } catch (IllegalArgumentException ignored) {}
    }

    @Test
    public void getValuesFromNonExistingColumn() {
        Table t = TestHelper.getTableWithAllColumnTypes();
        t.addEmptyRows(10);

        try { t.getBinaryByteArray(-1, 0);          fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException ignored) { }
        try { t.getBinaryByteArray(-10, 0);         fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException ignored) { }
        try { t.getBinaryByteArray(9, 0);           fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException ignored) { }

        try { t.getBoolean(-1, 0);                  fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException ignored) { }
        try { t.getBoolean(-10, 0);                 fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException ignored) { }
        try { t.getBoolean(9, 0);                   fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException ignored) { }

        try { t.getDate(-1, 0);                     fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException ignored) { }
        try { t.getDate(-10, 0);                    fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException ignored) { }
        try { t.getDate(9, 0);                      fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException ignored) { }

        try { t.getDouble(-1, 0);                   fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException ignored) { }
        try { t.getDouble(-10, 0);                  fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException ignored) { }
        try { t.getDouble(9, 0);                    fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException ignored) { }

        try { t.getFloat(-1, 0);                    fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException ignored) { }
        try { t.getFloat(-10, 0);                   fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException ignored) { }
        try { t.getFloat(9, 0);                     fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException ignored) { }

        try { t.getLong(-1, 0);                     fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException ignored) { }
        try { t.getLong(-10, 0);                    fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException ignored) { }
        try { t.getLong(9, 0);                      fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException ignored) { }

        try { t.getString(-1, 0);                   fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException ignored) { }
        try { t.getString(-10, 0);                  fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException ignored) { }
        try { t.getString(9, 0);                    fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException ignored) { }
    }

    @Test
    public void getNonExistingColumn() {
        Table t = new Table();
        t.addColumn(RealmFieldType.INTEGER, "int");

        assertEquals(-1, t.getColumnIndex("non-existing column"));
        try { t.getColumnIndex(null); fail("column name null"); } catch (IllegalArgumentException ignored) { }
    }

    @Test
    public void setNulls() {
        Table t = new Table();
        t.addColumn(RealmFieldType.STRING, "");
        t.addColumn(RealmFieldType.DATE, "");
        t.addColumn(RealmFieldType.BINARY, "");
        t.add("String val", new Date(), new byte[]{1, 2, 3});

        try { t.setString(0, 0, null);  fail("null string not allowed"); } catch (IllegalArgumentException ignored) { }
        try { t.setDate(1, 0, null);    fail("null Date not allowed"); } catch (IllegalArgumentException ignored) { }
    }

    @Test
    public void addNegativeEmptyRows() {
        Table t = new Table();
        t.addColumn(RealmFieldType.STRING, "colName");

        try { t.addEmptyRows(-1); fail("Argument is negative"); } catch (IllegalArgumentException ignored) { }
    }

    @Test
    public void getName() {
        String TABLE_NAME = "tableName";
        RealmConfiguration configuration = configFactory.createConfiguration();
        Realm.deleteRealm(configuration);

        SharedRealm sharedRealm = SharedRealm.getInstance(configuration);

        // Write transaction must be run so we are sure a db exists with the correct table
        sharedRealm.beginTransaction();
        sharedRealm.getTable(TABLE_NAME);
        sharedRealm.commitTransaction();

        Table table = sharedRealm.getTable(TABLE_NAME);
        assertEquals(TABLE_NAME, table.getName());
    }

    @Test
    public void shouldThrowWhenSetIndexOnWrongRealmFieldType() {
        for (long colIndex = 0; colIndex < t.getColumnCount(); colIndex++) {

            // All types supported addSearchIndex and removeSearchIndex
            boolean exceptionExpected = (
                            t.getColumnType(colIndex) != RealmFieldType.STRING &&
                            t.getColumnType(colIndex) != RealmFieldType.INTEGER &&
                            t.getColumnType(colIndex) != RealmFieldType.BOOLEAN &&
                            t.getColumnType(colIndex) != RealmFieldType.DATE);

            // Try to addSearchIndex()
            try {
                t.addSearchIndex(colIndex);
                if (exceptionExpected) {
                    fail("Expected exception for colIndex " + colIndex);
                }
            } catch (IllegalArgumentException ignored) {
            }

            // Try to removeSearchIndex()
            try {
                // Currently core will do nothing if the column doesn't have a search index
                t.removeSearchIndex(colIndex);
                if (exceptionExpected) {
                    fail("Expected exception for colIndex " + colIndex);
                }
            } catch (IllegalArgumentException ignored) {
            }


            // Try to hasSearchIndex() for all columnTypes
            t.hasSearchIndex(colIndex);
        }
    }

    @Test
    public void columnName() {
        Table t = new Table();
        try { t.addColumn(RealmFieldType.STRING, "I am 64 characters.............................................."); fail("Only 63 characters supported"); } catch (IllegalArgumentException ignored) { }
        t.addColumn(RealmFieldType.STRING, "I am 63 characters.............................................");
    }

    @Test
    public void tableNumbers() {
        Table t = new Table();
        t.addColumn(RealmFieldType.INTEGER, "intCol");
        t.addColumn(RealmFieldType.DOUBLE, "doubleCol");
        t.addColumn(RealmFieldType.FLOAT, "floatCol");
        t.addColumn(RealmFieldType.STRING, "StringCol");

        // Add 3 rows of data with same values in each column
        t.add(1, 2.0d, 3.0f, "s1");
        t.add(1, 2.0d, 3.0f, "s1");
        t.add(1, 2.0d, 3.0f, "s1");

        // Add other values
        t.add(10, 20.0d, 30.0f, "s10");
        t.add(100, 200.0d, 300.0f, "s100");
        t.add(1000, 2000.0d, 3000.0f, "s1000");

        // Count instances of values added in the first 3 rows
        assertEquals(3, t.count(0, 1));
        assertEquals(3, t.count(1, 2.0d));
        assertEquals(3, t.count(2, 3.0f));
        assertEquals(3, t.count(3, "s1"));

        assertEquals(3, t.findAllDouble(1, 2.0d).size());
        assertEquals(3, t.findAllFloat(2, 3.0f).size());

        assertEquals(3, t.findFirstDouble(1, 20.0d)); // Find rows index for first double value of 20.0 in column 1
        assertEquals(4, t.findFirstFloat(2, 300.0f)); // Find rows index for first float value of 300.0 in column 2

        // Set double and float
        t.setDouble(1, 2, -2.0d);
        t.setFloat(2, 2, -3.0f);

        // Get double tests
        assertEquals(-2.0d, t.getDouble(1, 2));
        assertEquals(20.0d, t.getDouble(1, 3));
        assertEquals(200.0d, t.getDouble(1, 4));
        assertEquals(2000.0d, t.getDouble(1, 5));

        // Get float test
        assertEquals(-3.0f, t.getFloat(2, 2));
        assertEquals(30.0f, t.getFloat(2, 3));
        assertEquals(300.0f, t.getFloat(2, 4));
        assertEquals(3000.0f, t.getFloat(2, 5));
    }

    @Test
    public void maximumDate() {

        Table table = new Table();
        table.addColumn(RealmFieldType.DATE, "date");

        table.add(new Date(0));
        table.add(new Date(10000));
        table.add(new Date(1000));

        assertEquals(new Date(10000), table.maximumDate(0));

    }

    @Test
    public void minimumDate() {

        Table table = new Table();
        table.addColumn(RealmFieldType.DATE, "date");

        table.add(new Date(10000));
        table.add(new Date(0));
        table.add(new Date(1000));

        assertEquals(new Date(0), table.minimumDate(0));

    }

    // testing the migration of a string column to be nullable.
    @Test
    public void convertToNullable() {
        RealmFieldType[] columnTypes = {RealmFieldType.BOOLEAN, RealmFieldType.DATE, RealmFieldType.DOUBLE,
                RealmFieldType.FLOAT, RealmFieldType.INTEGER, RealmFieldType.BINARY, RealmFieldType.STRING};
        for (RealmFieldType columnType : columnTypes) {
            // testing various combinations of column names and nullability
            String[] columnNames = {"foobar", "__TMP__0"};
            for (boolean nullable : new boolean[]{Table.NOT_NULLABLE, Table.NULLABLE}) {
                for (String columnName : columnNames) {
                    Table table = new Table();
                    long colIndex = table.addColumn(columnType, columnName, nullable);
                    table.addColumn(RealmFieldType.BOOLEAN, "bool");
                    table.addEmptyRow();
                    if (columnType == RealmFieldType.BOOLEAN) {
                        table.setBoolean(colIndex, 0, true);
                    } else if (columnType == RealmFieldType.DATE) {
                        table.setDate(colIndex, 0, new Date(0));
                    } else if (columnType == RealmFieldType.DOUBLE) {
                        table.setDouble(colIndex, 0, 1.0);
                    } else if (columnType == RealmFieldType.FLOAT) {
                        table.setFloat(colIndex, 0, 1.0f);
                    } else if (columnType == RealmFieldType.INTEGER) {
                        table.setLong(colIndex, 0, 1);
                    } else if (columnType == RealmFieldType.BINARY) {
                        table.setBinaryByteArray(colIndex, 0, new byte[]{0});
                    } else if (columnType == RealmFieldType.STRING) {
                        table.setString(colIndex, 0, "Foo");
                    }
                    try {
                        table.addEmptyRow();
                        if (columnType == RealmFieldType.BINARY) {
                            table.setBinaryByteArray(colIndex, 1, null);
                        } else if (columnType == RealmFieldType.STRING) {
                            table.setString(colIndex, 1, null);
                        } else {
                            table.getCheckedRow(1).setNull(colIndex);
                        }

                        if (!nullable) {
                            fail();
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                    table.removeLast();
                    assertEquals(1, table.size());

                    table.convertColumnToNullable(colIndex);
                    assertTrue(table.isColumnNullable(colIndex));
                    assertEquals(1, table.size());
                    assertEquals(2, table.getColumnCount());
                    assertTrue(table.getColumnIndex(columnName) >= 0);
                    assertEquals(colIndex, table.getColumnIndex(columnName));

                    table.addEmptyRow();
                    if (columnType == RealmFieldType.BINARY) {
                        table.setBinaryByteArray(colIndex, 0, null);
                    } else if (columnType == RealmFieldType.STRING) {
                        table.setString(colIndex, 0, null);
                    } else {
                        table.getCheckedRow(0).setNull(colIndex);
                    }

                    assertEquals(2, table.size());

                    if (columnType == RealmFieldType.BINARY) {
                        assertNull(table.getBinaryByteArray(colIndex, 1));
                    } else if (columnType == RealmFieldType.STRING) {
                        assertNull(table.getString(colIndex, 1));
                    } else {
                        assertTrue(table.getUncheckedRow(1).isNull(colIndex));
                    }
                }
            }
        }
    }

    @Test
    public void convertToNotNullable() {
        RealmFieldType[] columnTypes = {RealmFieldType.BOOLEAN, RealmFieldType.DATE, RealmFieldType.DOUBLE,
                RealmFieldType.FLOAT, RealmFieldType.INTEGER, RealmFieldType.BINARY, RealmFieldType.STRING};
        for (RealmFieldType columnType : columnTypes) {
            // testing various combinations of column names and nullability
            String[] columnNames = {"foobar", "__TMP__0"};
            for (boolean nullable : new boolean[]{Table.NOT_NULLABLE, Table.NULLABLE}) {
                for (String columnName : columnNames) {
                    Table table = new Table();
                    long colIndex = table.addColumn(columnType, columnName, nullable);
                    table.addColumn(RealmFieldType.BOOLEAN, "bool");
                    table.addEmptyRow();
                    if (columnType == RealmFieldType.BOOLEAN)
                        table.setBoolean(colIndex, 0, true);
                    else if (columnType == RealmFieldType.DATE)
                        table.setDate(colIndex, 0, new Date(1));
                    else if (columnType == RealmFieldType.DOUBLE)
                        table.setDouble(colIndex, 0, 1.0);
                    else if (columnType == RealmFieldType.FLOAT)
                        table.setFloat(colIndex, 0, 1.0f);
                    else if (columnType == RealmFieldType.INTEGER)
                        table.setLong(colIndex, 0, 1);
                    else if (columnType == RealmFieldType.BINARY)
                        table.setBinaryByteArray(colIndex, 0, new byte[]{0});
                    else if (columnType == RealmFieldType.STRING)
                        table.setString(colIndex, 0, "Foo");
                    try {
                        table.addEmptyRow();
                        if (columnType == RealmFieldType.BINARY) {
                            table.setBinaryByteArray(colIndex, 1, null);
                        } else if (columnType == RealmFieldType.STRING) {
                            table.setString(colIndex, 1, null);
                        } else {
                            table.getCheckedRow(1).setNull(colIndex);
                        }

                        if (!nullable) {
                            fail();
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                    assertEquals(2, table.size());

                    table.convertColumnToNotNullable(colIndex);
                    assertFalse(table.isColumnNullable(colIndex));
                    assertEquals(2, table.size());
                    assertEquals(2, table.getColumnCount());
                    assertTrue(table.getColumnIndex(columnName) >= 0);
                    assertEquals(colIndex, table.getColumnIndex(columnName));

                    table.addEmptyRow();
                    try {
                        if (columnType == RealmFieldType.BINARY) {
                            table.setBinaryByteArray(colIndex, 0, null);
                        } else if (columnType == RealmFieldType.STRING) {
                            table.setString(colIndex, 0, null);
                        } else {
                            table.getCheckedRow(0).setNull(colIndex);
                        }
                        if (!nullable) {
                            fail();
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                    table.removeLast();
                    assertEquals(2, table.size());

                    if (columnType == RealmFieldType.BINARY) {
                        assertNotNull(table.getBinaryByteArray(colIndex, 1));
                    } else if (columnType == RealmFieldType.STRING) {
                        assertNotNull(table.getString(colIndex, 1));
                        assertEquals("", table.getString(colIndex, 1));
                    } else {
                        assertFalse(table.getUncheckedRow(1).isNull(colIndex));
                        if (columnType == RealmFieldType.BOOLEAN)
                            assertEquals(false, table.getBoolean(colIndex, 1));
                        else if (columnType == RealmFieldType.DATE)
                            assertEquals(0, table.getDate(colIndex, 1).getTime());
                        else if (columnType == RealmFieldType.DOUBLE)
                            assertEquals(0.0, table.getDouble(colIndex, 1));
                        else if (columnType == RealmFieldType.FLOAT)
                            assertEquals(0.0f, table.getFloat(colIndex, 1));
                        else if (columnType == RealmFieldType.INTEGER)
                            assertEquals(0, table.getLong(colIndex, 1));
                    }
                }
            }
        }
    }

    // add column and read back if it is nullable or not
    @Test
    public void isNullable() {
        Table table = new Table();
        table.addColumn(RealmFieldType.STRING, "string1", Table.NOT_NULLABLE);
        table.addColumn(RealmFieldType.STRING, "string2", Table.NULLABLE);

        assertFalse(table.isColumnNullable(0));
        assertTrue(table.isColumnNullable(1));
    }
}

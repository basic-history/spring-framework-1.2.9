/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jdbc.core;

/**
 * Subclass of SqlParameter to represent an output parameter.
 * No additional properties: instanceof will be used to check
 * for such types.
 *
 * <p>Output parameters - like all stored procedure parameters -
 * must have names.
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @see SqlReturnResultSet
 */
public class SqlOutParameter extends ResultSetSupportingSqlParameter {

	private SqlReturnType sqlReturnType;


	/**
	 * Create a new SqlOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 */
	public SqlOutParameter(String name, int sqlType) {
		super(name, sqlType);
	}

	/**
	 * Create a new SqlOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param typeName the type name of the parameter (optional)
	 */
	public SqlOutParameter(String name, int sqlType, String typeName) {
		super(name, sqlType, typeName);
	}

	/**
	 * Create a new SqlOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param typeName the type name of the parameter (optional)
	 * @param sqlReturnType custom value handler for complex type (optional)
	 */
	public SqlOutParameter(String name, int sqlType, String typeName, SqlReturnType sqlReturnType) {
		super(name, sqlType, typeName);
		this.sqlReturnType = sqlReturnType;
	}

	/**
	 * Create a new SqlOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rse ResultSetExtractor to use for parsing the ResultSet
	 */
	public SqlOutParameter(String name, int sqlType, ResultSetExtractor rse) {
		super(name, sqlType, rse);
	}

	/**
	 * Create a new SqlOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rch RowCallbackHandler to use for parsing the ResultSet
	 */
	public SqlOutParameter(String name, int sqlType, RowCallbackHandler rch) {
		super(name, sqlType, rch);
	}

	/**
	 * Create a new SqlOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rm RowMapper to use for parsing the ResultSet
	 */
	public SqlOutParameter(String name, int sqlType, RowMapper rm) {
		super(name, sqlType, rm);
	}

	/**
	 * Create a new SqlOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rm RowMapper to use for parsing the ResultSet
	 * @param rowsExpected number of expected rows
	 */
	public SqlOutParameter(String name, int sqlType, RowMapper rm, int rowsExpected) {
		super(name, sqlType, rm, rowsExpected);
	}


	/**
	 * Return whether this parameter holds a custom return type.
	 */
	public boolean isReturnTypeSupported() {
		return (this.sqlReturnType != null);
	}

	/**
	 * Return the custom return type, if any.
	 */
	public SqlReturnType getSqlReturnType() {
		return sqlReturnType;
	}

}

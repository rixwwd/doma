package org.seasar.doma.jdbc.dialect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.seasar.doma.expr.ExpressionFunctions;

public class Db2DialectTest {

  @Test
  public void testExpressionFunctions_prefix() throws Exception {
    Db2Dialect dialect = new Db2Dialect();
    ExpressionFunctions functions = dialect.getExpressionFunctions();
    assertEquals("a$$a$%a$_a$％a$＿%", functions.prefix("a$a%a_a％a＿"));
  }

  @Test
  public void testExpressionFunctions_prefix_escape() throws Exception {
    Db2Dialect dialect = new Db2Dialect();
    ExpressionFunctions functions = dialect.getExpressionFunctions();
    assertEquals("a!!a!%a!_a!％a!＿%", functions.prefix("a!a%a_a％a＿", '!'));
  }

  @Test
  public void testExpressionFunctions_prefix_escapeWithDefault() throws Exception {
    Db2Dialect dialect = new Db2Dialect();
    ExpressionFunctions functions = dialect.getExpressionFunctions();
    assertEquals("a\\\\a\\%a\\_a\\％a\\＿%", functions.prefix("a\\a%a_a％a＿", '\\'));
  }
}

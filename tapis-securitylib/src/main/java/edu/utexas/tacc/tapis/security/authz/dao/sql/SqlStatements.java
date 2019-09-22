package edu.utexas.tacc.tapis.security.authz.dao.sql;

/** This class centralizes all SQL statements used in the Tapis Security Kernel 
 * for authorization.  The statements returned are ready for preparation and, 
 * when all placeholders are properly bound, execution.
 * 
 * @author rich
 *
 */
public class SqlStatements
{
  /* ---------------------------------------------------------------------- */
  /* sk_role:                                                               */
  /* ---------------------------------------------------------------------- */
  // Get all rows.
  public static final String SELECT_SKROLE =
          "SELECT id, tenant, name, description, created, createdby, updated, updatedby"
          + " FROM sk_role";
  
  // Role statements.
  public static final String ROLE_SELECT_BY_NAME = 
      "SELECT id, tenant, name, description FROM sk_role where tenant = ? AND name = ?";
  public static final String ROLE_SELECT_EXTENDED_BY_NAME = 
      "SELECT id, tenant, name, description, created, createdby, updated, updatedby FROM sk_role where tenant = ? AND name = ?";
  public static final String ROLE_SELECT_BY_ID = 
      "SELECT id, tenant, name, description FROM sk_role where tenant = ? AND id = ?";
  public static final String ROLE_SELECT_EXTENDED_BY_ID = 
      "SELECT id, tenant, name, description, created, createdby, updated, updatedby FROM sk_role where tenant = ? AND id = ?";
  public static final String ROLE_INSERT = 
      "INSERT INTO sk_role (tenant, name, description, createdby, updatedby) VALUES (?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";
  public static final String ROLE_SELECT_ID_BY_NAME =
      "SELECT id FROM sk_role where tenant = ? AND name = ?";
  public static final String ROLE_DELETE_BY_ID =
      "DELETE FROM sk_role where tenant = ? AND id = ?";
  public static final String ROLE_DELETE_BY_NAME =
      "DELETE FROM sk_role where tenant = ? AND name = ?";
  public static final String ROLE_UPDATE = 
      "UPDATE sk_role SET name = ?, description = ?, updated = ?, updatedby = ? where tenant = ? AND id = ?";
  
  /* ---------------------------------------------------------------------- */
  /* sk_role_permission:                                                    */
  /* ---------------------------------------------------------------------- */
  // Get all rows.
  public static final String SELECT_SKROLEPERMISSION =
      "SELECT id, tenant, role_id, permission, created, createdby, updated, updatedby"
      + " FROM sk_role_permission";
  
  // The following select statement grabs the role id from the sk_role table after 
  // guaranteeing that the role's tenant is the expected one.    
  public static final String ROLE_ADD_PERMISSION =
      "INSERT INTO sk_role_permission (tenant, role_id, permission, createdby, updatedby) " +
      "select ?, r.id, ?, ?, ? from sk_role r where r.tenant = ? and r.id = ? " +
      "ON CONFLICT DO NOTHING";        
  
  public static final String ROLE_REMOVE_PERMISSION =
      "DELETE FROM sk_role_permission where tenant = ? and role_id = ? and permission = ?";

  /* ---------------------------------------------------------------------- */
  /* sk_role_tree:                                                          */
  /* ---------------------------------------------------------------------- */
  // Get all rows.
  public static final String SELECT_SKROLETREE =
      "SELECT id, tenant, parent_role_id, child_role_id, created, createdby, updated, updatedby"
      + " FROM sk_role_tree";
  
  // The following select statement only grabs the tenant and child role id from the 
  // sk_role table, but uses the parent role id, createdby and updatedby constants 
  // passed in from the caller. The parent and child tenants are guaranteed to match
  // because the last clause matches the parent role's tenant.
  public static final String ROLE_ADD_CHILD_ROLE_BY_NAME =
      "INSERT INTO sk_role_tree (tenant, parent_role_id, child_role_id, createdby, updatedby) " +
      "select r.tenant, ?, r.id, ?, ? from sk_role r where r.tenant = ? and r.name = ? " +
      "and r.tenant = (select tenant from sk_role where id = ?) " + // enforce tenant conformance
      "ON CONFLICT DO NOTHING";
  public static final String ROLE_REMOVE_CHILD_ROLE_BY_NAME =
      "DELETE FROM sk_role_tree where tenant = ? and parent_role_id = ? and " +
      "child_role_id = (select r.id from sk_role r where r.tenant = ? and r.name = ?)";
  
  // Child role name retrieval in alphabetic order.
  public static final String ROLE_GET_IMMEDIATE_CHILD_ROLE_NAMES =
      "SELECT r.name from sk_role r, sk_role_tree rt " +
      "where r.tenant = ? and r.id = rt.child_role_id and rt.parent_role_id = ? " +
      "order by r.name";
  
  // Parent role name retrieval in alphabetic order.
  public static final String ROLE_GET_IMMEDIATE_PARENT_ROLE_NAMES =
      "SELECT r.name from sk_role r, sk_role_tree rt " +
      "where r.tenant = ? and r.id = rt.parent_role_id and rt.child_role_id = ? " +
      "order by r.name";
  
  // This recursive query retrieves all the roles names that are descendants
  // of the specified role (the role whose id parameter is passed in).  The
  // query returns the child names in alphabetic order.  The application  
  // guards against introducing a cycle in the graph to avoid infinite loops. 
  // Union Distinct is used to remove duplicates since as an acyclic graph, 
  // the role hierarchy allows a node to have multiple parents.
  //
  // NOTE: This postgres-specific syntax needs to be moved to
  //       a postgres file when another database is supported.
  public static final String ROLE_GET_DESCENDANT_NAMES_FOR_PARENT_ID =
      "WITH RECURSIVE children AS ( " +
      "SELECT child_role_id FROM sk_role_tree WHERE parent_role_id = ? " +
      "UNION DISTINCT " +
      "SELECT a.child_role_id FROM sk_role_tree a, children b " +
        "WHERE a.parent_role_id = b.child_role_id " +
      ") " +
      "SELECT sk_role.name FROM children, sk_role " +
        "WHERE sk_role.id = children.child_role_id " +
        "ORDER BY sk_role.name";
  
  // Given a child role id, get all its ancestor role names.
  public static final String ROLE_GET_ANCESTOR_NAMES_FOR_CHILD_ID =
      "WITH RECURSIVE children AS ( " +
      "SELECT parent_role_id FROM sk_role_tree WHERE child_role_id = ? " +
      "UNION DISTINCT " +
      "SELECT a.parent_role_id FROM sk_role_tree a, children b " +
        "WHERE a.child_role_id = b.parent_role_id " +
      ") " +
      "SELECT sk_role.name FROM children, sk_role " +
        "WHERE sk_role.id = children.parent_role_id " +
        "ORDER BY sk_role.name";
  
  // Given a role, find all permissions assigned to that role
  // and the transitive closure of all its descendants.  The 
  // WITH statement retrieves all the descendant role ids.  The
  // query that follows the WITH is a UNION.  The first part of 
  // this UNION calculates the permission values assigned to the 
  // descendant roles discovered in the preceding recursive calls.  
  // The second part of the UNION retrieves the permission values
  // assigned to the parent role.
  public static final String ROLE_GET_TRANSITIVE_PERMISSIONS =
      "WITH RECURSIVE children AS ( " +
      "SELECT child_role_id FROM sk_role_tree WHERE parent_role_id = ? " +
      "UNION DISTINCT " +
      "SELECT a.child_role_id FROM sk_role_tree a, children b " +
        "WHERE a.parent_role_id = b.child_role_id " +
      ") " +
      "SELECT DISTINCT rp1.permission AS outperm " +
        "FROM children, sk_role_permission rp1 " +
        "WHERE rp1.role_id = children.child_role_id " +
      "UNION DISTINCT " +
      "SELECT DISTINCT rp2.permission AS outperm " +
        "FROM sk_role_permission rp2 " +
        "WHERE rp2.role_id = ? " +
      "ORDER BY outperm";

  /* ---------------------------------------------------------------------- */
  /* sk_user_role:                                                          */
  /* ---------------------------------------------------------------------- */
  // Get all rows.
  public static final String SELECT_SKUSERROLE =
      "SELECT id, tenant, user_name, role_id, created, createdby, updated, updatedby"
      + " FROM sk_user_role";

  // If the role's tenant does not match the passed in tenant, the insert will fail.
  public static final String USER_ADD_ROLE_BY_ID =
      "INSERT INTO sk_user_role (tenant, user_name, role_id, createdby, updatedby) " +
      "select r.tenant, ?, ?, ?, ? from sk_role r where r.tenant = ? and r.id = ? " +
      "ON CONFLICT DO NOTHING";

  // Get the role ids assigned to user.
  public static final String USER_SELECT_ROLE_IDS =
      "SELECT role_id FROM sk_user_role WHERE tenant = ? and user_name = ?";
  
  // Get the role ids and the role names assigned to user.
  public static final String USER_SELECT_ROLE_IDS_AND_NAMES =
      "SELECT ur.role_id, r.name FROM sk_user_role ur, sk_role r " +
      "WHERE ur.role_id = r.id and ur.tenant = ? and ur.user_name = ?";

}

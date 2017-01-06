/**
 * generated by Xtext 2.10.0
 */
package org.thingml.xtext.thingML;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Parallel Region</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.thingml.xtext.thingML.ParallelRegion#getName <em>Name</em>}</li>
 *   <li>{@link org.thingml.xtext.thingML.ParallelRegion#getInitial <em>Initial</em>}</li>
 *   <li>{@link org.thingml.xtext.thingML.ParallelRegion#isHistory <em>History</em>}</li>
 *   <li>{@link org.thingml.xtext.thingML.ParallelRegion#getSubstate <em>Substate</em>}</li>
 * </ul>
 *
 * @see org.thingml.xtext.thingML.ThingMLPackage#getParallelRegion()
 * @model
 * @generated
 */
public interface ParallelRegion extends AnnotatedElement, Region
{
  /**
   * Returns the value of the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Name</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Name</em>' attribute.
   * @see #setName(String)
   * @see org.thingml.xtext.thingML.ThingMLPackage#getParallelRegion_Name()
   * @model
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link org.thingml.xtext.thingML.ParallelRegion#getName <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

  /**
   * Returns the value of the '<em><b>Initial</b></em>' reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Initial</em>' reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Initial</em>' reference.
   * @see #setInitial(State)
   * @see org.thingml.xtext.thingML.ThingMLPackage#getParallelRegion_Initial()
   * @model
   * @generated
   */
  State getInitial();

  /**
   * Sets the value of the '{@link org.thingml.xtext.thingML.ParallelRegion#getInitial <em>Initial</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Initial</em>' reference.
   * @see #getInitial()
   * @generated
   */
  void setInitial(State value);

  /**
   * Returns the value of the '<em><b>History</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>History</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>History</em>' attribute.
   * @see #setHistory(boolean)
   * @see org.thingml.xtext.thingML.ThingMLPackage#getParallelRegion_History()
   * @model
   * @generated
   */
  boolean isHistory();

  /**
   * Sets the value of the '{@link org.thingml.xtext.thingML.ParallelRegion#isHistory <em>History</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>History</em>' attribute.
   * @see #isHistory()
   * @generated
   */
  void setHistory(boolean value);

  /**
   * Returns the value of the '<em><b>Substate</b></em>' containment reference list.
   * The list contents are of type {@link org.thingml.xtext.thingML.State}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Substate</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Substate</em>' containment reference list.
   * @see org.thingml.xtext.thingML.ThingMLPackage#getParallelRegion_Substate()
   * @model containment="true"
   * @generated
   */
  EList<State> getSubstate();

} // ParallelRegion

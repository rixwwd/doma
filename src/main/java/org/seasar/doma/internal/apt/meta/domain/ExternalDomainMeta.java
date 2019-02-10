package org.seasar.doma.internal.apt.meta.domain;

import static org.seasar.doma.internal.util.AssertionUtil.assertNotNull;

import java.util.List;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.seasar.doma.internal.apt.cttype.BasicCtType;
import org.seasar.doma.internal.apt.def.TypeParametersDef;
import org.seasar.doma.internal.apt.meta.TypeElementMeta;

public class ExternalDomainMeta implements TypeElementMeta {

  protected final TypeElement converterElement;

  private BasicCtType basicCtType;

  private TypeMirror valueType;

  private TypeElement typeElement;

  private TypeParametersDef typeParametersDef;

  public ExternalDomainMeta(TypeElement converterElement) {
    assertNotNull(converterElement);
    this.converterElement = converterElement;
  }

  public TypeElement getConverterElement() {
    return converterElement;
  }

  public BasicCtType getBasicCtType() {
    return basicCtType;
  }

  public void setBasicCtType(BasicCtType basicCtType) {
    this.basicCtType = basicCtType;
  }

  public TypeMirror getValueType() {
    return valueType;
  }

  public void setValueType(TypeMirror valueType) {
    this.valueType = valueType;
  }

  public TypeElement getTypeElement() {
    return typeElement;
  }

  public void setTypeElement(TypeElement typeElement) {
    this.typeElement = typeElement;
  }

  public Name getQualifiedName() {
    return typeElement.getQualifiedName();
  }

  public void setTypeParametersDef(TypeParametersDef typeParametersDef) {
    this.typeParametersDef = typeParametersDef;
  }

  public List<String> getTypeVariables() {
    return typeParametersDef.getTypeVariables();
  }

  public List<String> getTypeParameters() {
    return typeParametersDef.getTypeParameters();
  }

  public TypeMirror getType() {
    return typeElement.asType();
  }

  public boolean isParameterized() {
    return !typeElement.getTypeParameters().isEmpty();
  }

  @Override
  public boolean isError() {
    return false;
  }
}

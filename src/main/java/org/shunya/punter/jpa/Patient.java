package org.shunya.punter.jpa;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="MyPatient", uniqueConstraints=
@UniqueConstraint(columnNames = {"firstName", "birthDate", "primaryIdentifier", "gender"}))
public class Patient {
    @Id
    @GenericGenerator(name="table-hilo-generator", strategy="org.hibernate.id.TableHiLoGenerator",
            parameters={@Parameter(value="hibernate_id_generation", name="patient_hilo")})
    @GeneratedValue(generator="table-hilo-generator")
    long id;
    int deptId;
    String patientType;
    String name;
    String lastName;
    String firstName;
    String middleName;
    String suffix;
    String addressLine1;
    String addressLine2;
    String city;
    String state;
    String postalCode;
    String country;
    Date birthDate;
    Date deathDate;
    String gender;
    String memberId;
    String groupNumber;
    String relationshipToSubscriber;
    String subscriberName;
    String subscriberLastName;
    String subscriberFirstName;
    String subscriberMiddleName;
    String subscriberSuffix;
    String primaryIdentifier;
    String primaryQualifier;

    public String getPrimaryIdentifier() {
        return primaryIdentifier;
    }

    public void setPrimaryIdentifier(String primaryIdentifier) {
        this.primaryIdentifier = primaryIdentifier;
    }

    public String getPrimaryQualifier() {
        return primaryQualifier;
    }

    public void setPrimaryQualifier(String primaryQualifier) {
        this.primaryQualifier = primaryQualifier;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public String getPatientType() {
        return patientType;
    }

    public void setPatientType(String patientType) {
        this.patientType = patientType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Date getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(String groupNumber) {
        this.groupNumber = groupNumber;
    }

    public String getRelationshipToSubscriber() {
        return relationshipToSubscriber;
    }

    public void setRelationshipToSubscriber(String relationshipToSubscriber) {
        this.relationshipToSubscriber = relationshipToSubscriber;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    public String getSubscriberLastName() {
        return subscriberLastName;
    }

    public void setSubscriberLastName(String subscriberLastName) {
        this.subscriberLastName = subscriberLastName;
    }

    public String getSubscriberFirstName() {
        return subscriberFirstName;
    }

    public void setSubscriberFirstName(String subscriberFirstName) {
        this.subscriberFirstName = subscriberFirstName;
    }

    public String getSubscriberMiddleName() {
        return subscriberMiddleName;
    }

    public void setSubscriberMiddleName(String subscriberMiddleName) {
        this.subscriberMiddleName = subscriberMiddleName;
    }

    public String getSubscriberSuffix() {
        return subscriberSuffix;
    }

    public void setSubscriberSuffix(String subscriberSuffix) {
        this.subscriberSuffix = subscriberSuffix;
    }

    //"PatientType","Name","LastName","FirstName","MiddleName","Suffix","AddressLine1","AddressLine2","City","State","PostalCode","Country","BirthDate","DeathDate","Gender","MemberID","GroupNumber","RelationshipToSubscriber","SubscriberName","SubscriberLastName","SubscriberFirstName","SubscriberMiddleName","SubscriberSuffix"
    //        "LACounty","SUKHDEV RANDIV","RANDIV","SUKHDEV",,"","1 SOLDIERS FIELD PARK #213","3 WESTERN AVENUE","Los Angeles","CA","90001","","01/01/1960","","M","533D974092","2303399","18","SUKHDEV RANDIV","RANDIV","SUKHDEV","D",""
}
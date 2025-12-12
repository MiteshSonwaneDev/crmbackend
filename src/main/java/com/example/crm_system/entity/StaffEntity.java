package com.example.crm_system.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "staffinfo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StaffEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many staff belong to one business
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String name;

    private String phoneNumber;

    private String designation;

    @Column(nullable = false)
    private String gender;  // Male / Female

    private String genderPreference; // No Preference / Male / Female

    // Multiple service preferences (checkboxes)
    @ElementCollection
    @CollectionTable(name = "staff_service_preferences", joinColumns = @JoinColumn(name = "staff_id"))
    @Column(name = "service")
    private List<String> servicePreferences;

    // ✅ Work calendar (days + timings) inside same entity
    @ElementCollection
    @CollectionTable(name = "staff_work_calendar", joinColumns = @JoinColumn(name = "staff_id"))
    private List<WorkDay> workCalendar;

    // ---------- Inner Class for WorkDay ----------
    @Embeddable
    public static class WorkDay {
        private String day;  // Monday, Tuesday, etc.
        private LocalTime openingHour;
        private LocalTime closingHour;

        // Getters & Setters
        public String getDay() {
            return day;
        }
        public void setDay(String day) {
            this.day = day;
        }
        public LocalTime getOpeningHour() {
            return openingHour;
        }
        public void setOpeningHour(LocalTime openingHour) {
            this.openingHour = openingHour;
        }
        public LocalTime getClosingHour() {
            return closingHour;
        }
        public void setClosingHour(LocalTime closingHour) {
            this.closingHour = closingHour;
        }
    }

    // ---------- Getters & Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Business getBusiness() { return business; }
    public void setBusiness(Business business) { this.business = business; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getGenderPreference() { return genderPreference; }
    public void setGenderPreference(String genderPreference) { this.genderPreference = genderPreference; }

    public List<String> getServicePreferences() { return servicePreferences; }
    public void setServicePreferences(List<String> servicePreferences) { this.servicePreferences = servicePreferences; }

    public List<WorkDay> getWorkCalendar() { return workCalendar; }
    public void setWorkCalendar(List<WorkDay> workCalendar) { this.workCalendar = workCalendar; }
}

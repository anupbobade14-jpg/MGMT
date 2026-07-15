package com.society.management.controller;

import com.society.management.dto.request.OwnerRequest;
import com.society.management.entity.*;
import com.society.management.exception.ApiException;
import com.society.management.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerRepository ownerRepo;
    private final FlatRepository flatRepo;
    private final UserRepository userRepo;
    private final FamilyMemberRepository familyRepo;
    private final VehicleRepository vehicleRepo;

    @GetMapping
    public List<Owner> list() { return ownerRepo.findAll(); }

    @GetMapping("/{id}")
    public Owner get(@PathVariable Long id) {
        return ownerRepo.findById(id).orElseThrow(() -> ApiException.notFound("Owner not found"));
    }

    @PostMapping
    public Owner create(@Valid @RequestBody OwnerRequest r) {
        Flat flat = flatRepo.findById(r.flatId())
                .orElseThrow(() -> ApiException.notFound("Flat not found"));
        User user = r.userId() != null ? userRepo.findById(r.userId()).orElse(null) : null;
        Owner o = Owner.builder()
                .fullName(r.fullName()).email(r.email()).phone(r.phone())
                .alternatePhone(r.alternatePhone()).emergencyContact(r.emergencyContact())
                .flat(flat).user(user)
                .moveInDate(r.moveInDate() != null ? LocalDate.parse(r.moveInDate()) : null)
                .primaryOwner(true).build();
        o = ownerRepo.save(o);
        if (flat.getOccupancy() == OccupancyStatus.VACANT) {
            flat.setOccupancy(OccupancyStatus.OCCUPIED);
            flatRepo.save(flat);
        }
        return o;
    }

    @PutMapping("/{id}")
    public Owner update(@PathVariable Long id, @Valid @RequestBody OwnerRequest r) {
        Owner o = get(id);
        o.setFullName(r.fullName()); o.setEmail(r.email()); o.setPhone(r.phone());
        o.setAlternatePhone(r.alternatePhone()); o.setEmergencyContact(r.emergencyContact());
        if (r.moveInDate() != null) o.setMoveInDate(LocalDate.parse(r.moveInDate()));
        return ownerRepo.save(o);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { ownerRepo.deleteById(id); }

    @GetMapping("/{id}/family")
    public List<FamilyMember> family(@PathVariable Long id) { return familyRepo.findByOwnerId(id); }

    @PostMapping("/{id}/family")
    public FamilyMember addFamily(@PathVariable Long id, @RequestBody FamilyMember fm) {
        fm.setOwner(get(id)); return familyRepo.save(fm);
    }

    @GetMapping("/{id}/vehicles")
    public List<Vehicle> vehicles(@PathVariable Long id) { return vehicleRepo.findByOwnerId(id); }

    @PostMapping("/{id}/vehicles")
    public Vehicle addVehicle(@PathVariable Long id, @RequestBody Vehicle v) {
        v.setOwner(get(id)); return vehicleRepo.save(v);
    }
}

package com.travel.place;

import com.travel.auth.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/places")
public class PlaceController {
  private final PlaceRepository places;
  private final UserRepository users;

  public PlaceController(PlaceRepository places, UserRepository users) {
    this.places = places;
    this.users = users;
  }

  public record Input(
      @NotBlank @Size(max = 120) String name,
      String country,
      String province,
      String city,
      String address,
      @NotNull Double longitude,
      @NotNull Double latitude,
      @NotNull LocalDate visitDate,
      @Pattern(regexp = "draft|published|archived") String status,
      String guideContent) {}

  private UserAccount user(AccountPrincipal p) {
    return users
        .findById(p.id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请重新登录"));
  }

  private Place own(Long id, AccountPrincipal p) {
    return places
        .findByIdAndUserId(id, p.id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "景点不存在"));
  }

  @GetMapping
  public List<PlaceView> list(
      @AuthenticationPrincipal AccountPrincipal p,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String country,
      @RequestParam(required = false) String city,
      @RequestParam(required = false) LocalDate date) {
    return places.findByUserIdAndStatusOrderByVisitDateDesc(p.id(), "published").stream()
        .filter(
            x ->
                q == null
                    || q.isBlank()
                    || String.join(
                            " ",
                            x.name,
                            x.address == null ? "" : x.address,
                            x.guideContent == null ? "" : x.guideContent)
                        .toLowerCase()
                        .contains(q.toLowerCase()))
        .filter(x -> country == null || country.equals(x.country))
        .filter(x -> city == null || city.equals(x.city))
        .filter(x -> date == null || date.equals(x.visitDate))
        .map(PlaceView::from)
        .toList();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PlaceView create(
      @AuthenticationPrincipal AccountPrincipal p, @Valid @RequestBody Input i) {
    Place x = apply(new Place(user(p)), i);
    return PlaceView.from(places.save(x));
  }

  @GetMapping("/{id}")
  public PlaceView get(@PathVariable Long id, @AuthenticationPrincipal AccountPrincipal p) {
    return PlaceView.from(own(id, p));
  }

  @PutMapping("/{id}")
  public PlaceView update(
      @PathVariable Long id,
      @AuthenticationPrincipal AccountPrincipal p,
      @Valid @RequestBody Input i) {
    return PlaceView.from(places.save(apply(own(id, p), i)));
  }

  @PostMapping("/{id}/publish")
  public PlaceView publish(@PathVariable Long id, @AuthenticationPrincipal AccountPrincipal p) {
    Place x = own(id, p);
    x.status = "published";
    return PlaceView.from(places.save(x));
  }

  @PostMapping("/{id}/archive")
  public PlaceView archive(@PathVariable Long id, @AuthenticationPrincipal AccountPrincipal p) {
    Place x = own(id, p);
    x.status = "archived";
    return PlaceView.from(places.save(x));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, @AuthenticationPrincipal AccountPrincipal p) {
    places.delete(own(id, p));
  }

  private Place apply(Place x, Input i) {
    x.name = i.name();
    x.country = i.country();
    x.province = i.province();
    x.city = i.city();
    x.address = i.address();
    x.longitude = BigDecimal.valueOf(i.longitude());
    x.latitude = BigDecimal.valueOf(i.latitude());
    x.visitDate = i.visitDate();
    x.status = i.status() == null ? "draft" : i.status();
    x.guideContent = i.guideContent();
    return x;
  }
}

package com.travel.place;
import java.time.LocalDate;
public record PlaceView(Long id,String name,String country,String province,String city,String address,double longitude,double latitude,LocalDate visitDate,String status,String guideContent){
 static PlaceView from(Place p){return new PlaceView(p.id,p.name,p.country,p.province,p.city,p.address,p.longitude,p.latitude,p.visitDate,p.status,p.guideContent);}
}

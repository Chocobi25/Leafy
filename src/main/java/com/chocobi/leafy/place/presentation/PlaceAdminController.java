package com.chocobi.leafy.place.presentation;

//다음 PR로 진행할 예정

public class PlaceAdminController {
    /*@DeleteMapping("/{placeId}")
    @PreAuthorize("hasRole('ADMIN')") // 관리자만 접근 가능
    public ResponseEntity<Map<String, String>> deletePlace(@PathVariable Long placeId) {
        try {
            placeService.deletePlace(placeId);
            return ResponseEntity.ok(Map.of("message", "장소가 성공적으로 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/image/{imageId}")
    @PreAuthorize("hasRole('ADMIN')") // 관리자만 접근 가능
    public ResponseEntity<Map<String, String>> deleteImage(@PathVariable Long imageId) {
        try {
            placeService.deleteImage(imageId);
            return ResponseEntity.ok(Map.of("message", "이미지가 성공적으로 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')") // 관리자만 접근 가능
    public ResponseEntity<List<PlaceDTO>> getAllPlaces() {
        List<PlaceDTO> places = placeService.getAllPlaces();
        return ResponseEntity.ok(places);
    }*/
}

# Fix Firestore NOT_FOUND Error by Synchronizing IDs

The application crashes when updating an appointment status because it uses the `hashCode()` of the Firestore document ID (an `Int`) as the document reference instead of the actual `String` ID. This plan refactors the `Appointment` entity to use `String` as its primary key, ensuring consistent identification between the local Room database and Firestore.

## Proposed Changes

### [Data Layer]

#### [MODIFY] [Appointment.kt](file:///C:/Users/paul5/Documents/Universidad/aplicaciones movil/proyecto/app/src/main/java/com/example/appagendamientocitas/data/local/entity/Appointment.kt)
- Change `id` from `Int` to `String`.
- Remove `autoGenerate = true` from `@PrimaryKey`.
- Set default value to an empty string.

#### [MODIFY] [AppointmentDao.kt](file:///C:/Users/paul5/Documents/Universidad/aplicaciones movil/proyecto/app/src/main/java/com/example/appagendamientocitas/data/local/dao/AppointmentDao.kt)
- Update all method signatures that use `id: Int` to use `id: String`.

#### [MODIFY] [FirestoreAppointmentRepository.kt](file:///C:/Users/paul5/Documents/Universidad/aplicaciones movil/proyecto/app/src/main/java/com/example/appagendamientocitas/data/repository/FirestoreAppointmentRepository.kt)
- **`createAppointment`**: Use `docRef.id` as the appointment ID before inserting locally.
- **`observeAll` / `observeByClient` / `getAppointmentsByDate`**: Map `doc.id` directly to `Appointment.id`.
- **`updateStatus`**: Update signature to `id: String` and use it directly to reference the Firestore document.

---

### [Domain Layer]

#### [MODIFY] [AppointmentRepository.kt](file:///C:/Users/paul5/Documents/Universidad/aplicaciones movil/proyecto/app/src/main/java/com/example/appagendamientocitas/domain/repository/AppointmentRepository.kt)
- Update `getById` and `updateStatus` signatures to use `id: String`.

#### [MODIFY] [AppointmentUseCases.kt](file:///C:/Users/paul5/Documents/Universidad/aplicaciones movil/proyecto/app/src/main/java/com/example/appagendamientocitas/domain/usecase/AppointmentUseCases.kt)
- Update `UpdateAppointmentStatusUseCase` signature to use `id: String`.

---

### [UI & Utilities Layer]

#### [MODIFY] [OwnerViewModel.kt](file:///C:/Users/paul5/Documents/Universidad/aplicaciones movil/proyecto/app/src/main/java/com/example/appagendamientocitas/ui/screens/owner/OwnerViewModel.kt)
- Update `approve`, `reject`, `complete`, and `setStatus` to handle `String` IDs.

#### [MODIFY] [AlarmScheduler.kt](file:///C:/Users/paul5/Documents/Universidad/aplicaciones movil/proyecto/app/src/main/java/com/example/appagendamientocitas/util/AlarmScheduler.kt)
- In `schedule`, use `appointment.id.hashCode()` for the `PendingIntent` request code and the notification ID `extra`.
- Update `cancel` signature to `id: String` and use its `hashCode()` to find and cancel the correct alarm.

## Verification Plan

### Automated Tests
- Run existing unit tests (if any) to check for compilation errors.
- Build the project to ensure all type mismatches are resolved.

### Manual Verification
- Deploy the app.
- Create a new appointment as a Client.
- Approve/Reject the appointment as an Owner.
- Verify in Logcat that no `NOT_FOUND` exception occurs and that the status updates correctly in both Room and Firestore.

<script>
  import RoleGuard from "$lib/components/RoleGuard.svelte";
  import ApiError from "$lib/components/ApiError.svelte";
  import { api } from "$lib/api.js";
  import { currentUser } from "$lib/stores/auth.js";
  import { toast } from "$lib/stores/toast.js";
  import { onMount } from "svelte";

  const user = $derived($currentUser);
  const userRole = $derived(user?.role?.toUpperCase() || "");

  // Page states
  let loading = $state(true);
  let saving = $state(false);
  let errorMessage = $state(null);
  let successMessage = $state(null);

  let hasExistingProfile = $state(false);
  let isEditing = $state(false);
  let originalProfile = {};

  let fullName = $state("");
  let email = $state("");
  let registrationNumber = $state("");

  // SME fields
  let enterpriseNumber = $state("");
  let registrationDate = $state("");
  let enterpriseType = $state("");
  let enterpriseStatus = $state("");
  let financialYearEnd = $state("");

  let idNumber = $state("");
  let educationalInstitution = $state("");
  let primaryFieldOfStudy = $state("");
  let cellphoneNumber = $state("");
  let isGraduated = $state(false);
  let graduationYear = $state("");

  const enterpriseTypes = [
    "Pty Ltd",
    "Close Corporation (CC)",
    "Sole Proprietorship",
    "Partnership",
  ];

  const enterpriseStatuses = ["Active", "Pending Verification", "Dormant"];

  const financialYearEnds = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
  ];

  const currentYear = new Date().getFullYear();

  const graduatedYears = Array.from(
    { length: currentYear - 2000 + 1 },
    (_, i) => String(currentYear - i),
  );

  const expectedYears = Array.from({ length: 10 }, (_, i) =>
    String(currentYear + i),
  );

  const yearOptions = $derived(isGraduated ? graduatedYears : expectedYears);

  function getFormValues() {
    return {
      registrationNumber,
      enterpriseNumber,
      registrationDate,
      enterpriseType,
      enterpriseStatus,
      financialYearEnd,
      idNumber,
      educationalInstitution,
      primaryFieldOfStudy,
      cellphoneNumber,
      isGraduated,
      graduationYear,
    };
  }

  function restoreFormValues(values) {
    registrationNumber = values.registrationNumber ?? "";
    enterpriseNumber = values.enterpriseNumber ?? "";
    registrationDate = values.registrationDate ?? "";
    enterpriseType = values.enterpriseType ?? "";
    enterpriseStatus = values.enterpriseStatus ?? "";
    financialYearEnd = values.financialYearEnd ?? "";

    idNumber = values.idNumber ?? "";
    educationalInstitution = values.educationalInstitution ?? "";
    primaryFieldOfStudy = values.primaryFieldOfStudy ?? "";
    cellphoneNumber = values.cellphoneNumber ?? "";
    isGraduated = values.isGraduated ?? false;
    graduationYear = values.graduationYear ?? "";
  }

  function detectExistingProfile(profile) {
    if (typeof profile.hasExistingProfile === "boolean") {
      return profile.hasExistingProfile;
    }

    if (profile.profileId != null || profile.profileCompleted === true) {
      return true;
    }

    const fields = [
      "registrationNumber",
      "enterpriseNumber",
      "registrationDate",
      "enterpriseType",
      "enterpriseStatus",
      "financialYearEnd",
      "idNumber",
      "educationalInstitution",
      "primaryFieldOfStudy",
      "cellphoneNumber",
      "graduationYear",
    ];

    return fields.some((field) => {
      const value = profile[field];

      return (
        value !== null && value !== undefined && String(value).trim() !== ""
      );
    });
  }

  async function loadProfile() {
    loading = true;
    errorMessage = null;

    try {
      if (typeof api.profile?.get !== "function") {
        throw new Error(
          "The profile API is not configured. Check api.profile.get() in $lib/api.js.",
        );
      }

      const response = await api.profile.get();

      if (!response || typeof response !== "object") {
        throw new Error("The server returned an invalid profile response.");
      }

      const profile = response.profile ?? response;

      fullName = user?.name ?? profile.fullName ?? profile.name ?? "";

      email = user?.email ?? profile.email ?? "";

      registrationNumber =
        profile.registrationNumber ?? user?.registrationNumber ?? "";

      enterpriseNumber = profile.enterpriseNumber ?? "";
      registrationDate = profile.registrationDate ?? "";
      enterpriseType = profile.enterpriseType ?? "";
      enterpriseStatus = profile.enterpriseStatus ?? "";
      financialYearEnd = profile.financialYearEnd ?? "";

      idNumber = profile.idNumber ?? "";
      educationalInstitution = profile.educationalInstitution ?? "";
      primaryFieldOfStudy = profile.primaryFieldOfStudy ?? "";
      cellphoneNumber = profile.cellphoneNumber ?? "";
      isGraduated = profile.isGraduated ?? false;
      graduationYear = profile.graduationYear ?? "";

      hasExistingProfile = detectExistingProfile(profile);

      isEditing = !hasExistingProfile;

      originalProfile = getFormValues();
    } catch (err) {
      console.error("Failed to load profile:", err);

      errorMessage = err?.message || "Failed to load your profile.";

    } finally {
      loading = false;
    }
  }

  function startEditing() {
    originalProfile = getFormValues();
    errorMessage = null;
    successMessage = null;
    isEditing = true;
  }

  function cancelEditing() {
    restoreFormValues(originalProfile);
    errorMessage = null;
    successMessage = null;
    isEditing = !hasExistingProfile;
  }

  async function handleSaveProfile(event) {
    event.preventDefault();

    if (saving || !isEditing) return;

    saving = true;
    errorMessage = null;
    successMessage = null;

    try {
      // Youth validation
      if (userRole === "YOUTH" || userRole === "ADMIN") {
        if (!/^\d{13}$/.test(idNumber)) {
          throw new Error(
            "South African ID Number must contain exactly 13 digits.",
          );
        }

        if (!/^\d{10}$/.test(cellphoneNumber)) {
          throw new Error("Cellphone number must contain exactly 10 digits.");
        }
      }

      // SME validation
      if (userRole === "SME" || userRole === "ADMIN") {
        if (!/^\d{4}\/\d{6}\/\d{2}$/.test(registrationNumber)) {
          throw new Error(
            "Registration number must follow the format 2021/123456/07.",
          );
        }
      }

      if (typeof api.profile?.update !== "function") {
        throw new Error("The profile update API is not configured.");
      }

      const payload =
        userRole === "SME"
          ? {
              registrationNumber: registrationNumber.trim(),
              enterpriseNumber: enterpriseNumber.trim(),
              registrationDate,
              enterpriseType,
              enterpriseStatus,
              financialYearEnd,
            }
          : userRole === "ADMIN"
            ? {
                registrationNumber: registrationNumber.trim(),
                enterpriseNumber: enterpriseNumber.trim(),
                registrationDate,
                enterpriseType,
                enterpriseStatus,
                financialYearEnd,
                idNumber: idNumber.trim(),
                educationalInstitution: educationalInstitution.trim(),
                primaryFieldOfStudy: primaryFieldOfStudy.trim(),
                cellphoneNumber: cellphoneNumber.trim(),
                isGraduated,
                graduationYear,
              }
            : {
                idNumber: idNumber.trim(),
                educationalInstitution: educationalInstitution.trim(),
                primaryFieldOfStudy: primaryFieldOfStudy.trim(),
                cellphoneNumber: cellphoneNumber.trim(),
                isGraduated,
                graduationYear,
              };

      await api.profile.update(payload);

      originalProfile = getFormValues();
      hasExistingProfile = true;
      isEditing = false;

      successMessage = "Your profile details have been saved.";
      toast.success("Profile saved successfully!");
    } catch (err) {
      console.error("Failed to save profile:", err);

      errorMessage = err?.message || "Failed to update your profile.";

      toast.error(errorMessage);
    } finally {
      saving = false;
    }
  }

  onMount(() => {
    loadProfile();
  });
</script>

<svelte:head>
  <title>Profile Settings | Shared Services Hub</title>
</svelte:head>

<RoleGuard allowedRoles={["SME", "YOUTH", "ADMIN"]}>
  <div class="mx-auto max-w-5xl px-4 py-8 sm:px-6 lg:px-8">
    <!-- Header -->
    <div
      class="border border-[#C8D4E8] bg-white p-8 shadow-xs mb-8 border-l-4 border-l-[#1446A0]"
    >
      <div
        class="inline-flex items-center gap-1.5 bg-[#EFF4FF] border border-[#C8D4E8] px-2.5 py-0.5 text-[10px] font-mono font-bold text-[#1446A0] uppercase mb-2"
      >
        {userRole === "SME"
          ? "SME COMPLIANCE LEDGER"
          : "YOUTH EXPERIENCE LEDGER"}
      </div>

      <h1
        class="font-serif text-3xl sm:text-4xl font-normal text-[#0D1B3E] tracking-tight"
      >
        {userRole === "SME"
          ? "Enterprise Profile Settings"
          : "Student & Skills Profile"}
      </h1>

      <p class="mt-1 text-sm text-[#465A7A] max-w-xl font-sans">
        {userRole === "SME"
          ? "Manage your official registration credentials and financial reporting periods."
          : "Manage your verified educational credentials and qualifications for corporate task matching."}
      </p>

      {#if !loading && !errorMessage && hasExistingProfile && !isEditing}
        <p class="mt-4 text-xs font-mono text-[#1A8A55]">
          Profile saved · Read-only mode
        </p>
      {:else if !loading && !errorMessage && !hasExistingProfile}
        <p class="mt-4 text-xs font-mono text-[#1446A0]">
          Complete your profile to continue.
        </p>
      {/if}
    </div>

    <ApiError message={errorMessage} onretry={loadProfile} />

    {#if successMessage}
      <div
        class="mb-6 border border-[#1A8A55] bg-[#F0FDF4] p-4 text-xs font-mono font-semibold text-[#1A8A55]"
      >
        ✓ {successMessage}
      </div>
    {/if}

    {#if loading}
      <div
        class="border border-[#C8D4E8] bg-white p-12 animate-pulse h-96"
      ></div>
    {:else if !errorMessage}
      <div class="border-2 border-[#1446A0] bg-white p-6 sm:p-8 shadow-md">
        <!-- Existing users can enable editing -->
        {#if hasExistingProfile && !isEditing}
          <div class="flex justify-end mb-6">
            <button
              type="button"
              onclick={startEditing}
              class="cape-btn-primary px-6 py-2.5 text-xs font-mono uppercase tracking-widest"
            >
              Edit Profile
            </button>
          </div>
        {/if}

        <form onsubmit={handleSaveProfile} class="space-y-6">
          <!-- Read-only account details -->
          <div
            class="grid grid-cols-1 md:grid-cols-2 gap-6 pb-6 border-b border-[#C8D4E8]"
          >
            <div>
              <label
                for="fullName"
                class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
              >
                Full Name (Locked)
              </label>

              <input
                id="fullName"
                type="text"
                disabled
                value={fullName}
                class="mt-1.5 block w-full px-4 py-2.5 text-sm font-sans bg-[#F2F5FA] border border-[#C8D4E8] text-[#6A84AC] cursor-not-allowed"
              />
            </div>

            <div>
              <label
                for="emailAddress"
                class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
              >
                Email Address (Locked)
              </label>

              <input
                id="emailAddress"
                type="email"
                disabled
                value={email}
                class="mt-1.5 block w-full px-4 py-2.5 text-sm font-mono bg-[#F2F5FA] border border-[#C8D4E8] text-[#6A84AC] cursor-not-allowed"
              />
            </div>
          </div>

          <!-- SME fields -->
          {#if userRole === "SME" || userRole === "ADMIN"}
            <div class="space-y-6">
              <h3
                class="font-serif text-xl text-[#0D1B3E] border-b border-[#C8D4E8] pb-2"
              >
                Business & Compliance Details
              </h3>

              <div>
                <label
                  for="registrationNumber"
                  class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                >
                  Registration Number *
                </label>

                <input
                  id="registrationNumber"
                  type="text"
                  disabled={!isEditing}
                  required
                  bind:value={registrationNumber}
                  class="mt-1.5 block w-full px-4 py-2.5 text-sm font-mono bg-[#F2F5FA] border border-[#C8D4E8] disabled:text-[#6A84AC] disabled:cursor-not-allowed"
                />
              </div>

              <div>
                <label
                  for="enterpriseNumber"
                  class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                >
                  Enterprise Number
                </label>

                <input
                  id="enterpriseNumber"
                  type="text"
                  disabled={!isEditing}
                  bind:value={enterpriseNumber}
                  class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm disabled:bg-[#F2F5FA] disabled:cursor-not-allowed"
                />
              </div>

              <div>
                <label
                  for="registrationDate"
                  class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                >
                  Registration Date
                </label>

                <input
                  id="registrationDate"
                  type="date"
                  disabled={!isEditing}
                  bind:value={registrationDate}
                  class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm disabled:bg-[#F2F5FA] disabled:cursor-not-allowed"
                />
              </div>

              <div>
                <label
                  for="enterpriseType"
                  class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                >
                  Enterprise Type *
                </label>

                <select
                  id="enterpriseType"
                  required
                  disabled={!isEditing}
                  bind:value={enterpriseType}
                  class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm disabled:bg-[#F2F5FA] disabled:cursor-not-allowed"
                >
                  <option value="">Select enterprise type</option>
                  {#each enterpriseTypes as t}
                    <option value={t}>{t}</option>
                  {/each}
                </select>
              </div>

              <div>
                <label
                  for="enterpriseStatus"
                  class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                >
                  Enterprise Status *
                </label>

                <select
                  id="enterpriseStatus"
                  required
                  disabled={!isEditing}
                  bind:value={enterpriseStatus}
                  class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm disabled:bg-[#F2F5FA] disabled:cursor-not-allowed"
                >
                  <option value="">Select status</option>
                  {#each enterpriseStatuses as s}
                    <option value={s}>{s}</option>
                  {/each}
                </select>
              </div>

              <div>
                <label
                  for="financialYearEnd"
                  class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                >
                  Financial Year End *
                </label>

                <select
                  id="financialYearEnd"
                  required
                  disabled={!isEditing}
                  bind:value={financialYearEnd}
                  class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm disabled:bg-[#F2F5FA] disabled:cursor-not-allowed"
                >
                  <option value="">Select month</option>
                  {#each financialYearEnds as month}
                    <option value={month}>{month}</option>
                  {/each}
                </select>
              </div>
            </div>
          {/if}

          <!-- Youth fields -->
          {#if userRole === "YOUTH" || userRole === "ADMIN"}
            <div class="space-y-6 pt-4">
              <h3
                class="font-serif text-xl text-[#0D1B3E] border-b border-[#C8D4E8] pb-2"
              >
                Student & Academic Credentials
              </h3>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label
                    for="idNumber"
                    class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                  >
                    ID Number (13 Digits) *
                  </label>

                  <input
                    id="idNumber"
                    type="text"
                    maxlength="13"
                    required
                    disabled={!isEditing}
                    bind:value={idNumber}
                    placeholder="Enter your ID number"
                    class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-mono disabled:bg-[#F2F5FA] disabled:cursor-not-allowed"
                  />
                </div>

                <div>
                  <label
                    for="cellphoneNumber"
                    class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                  >
                    Cellphone Number (10 Digits) *
                  </label>

                  <input
                    id="cellphoneNumber"
                    type="tel"
                    maxlength="10"
                    required
                    disabled={!isEditing}
                    bind:value={cellphoneNumber}
                    placeholder="e.g. 0821234567"
                    class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-mono disabled:bg-[#F2F5FA] disabled:cursor-not-allowed"
                  />
                </div>

                <div>
                  <label
                    for="educationalInstitution"
                    class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                  >
                    Educational Institution *
                  </label>

                  <input
                    id="educationalInstitution"
                    type="text"
                    required
                    disabled={!isEditing}
                    bind:value={educationalInstitution}
                    placeholder="Your institution"
                    class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-mono disabled:bg-[#F2F5FA] disabled:cursor-not-allowed"
                  />
                </div>

                <div>
                  <label
                    for="primaryFieldOfStudy"
                    class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                  >
                    Primary Field of Study *
                  </label>

                  <input
                    id="primaryFieldOfStudy"
                    type="text"
                    required
                    disabled={!isEditing}
                    bind:value={primaryFieldOfStudy}
                    placeholder="Your field of study"
                    class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-mono disabled:bg-[#F2F5FA] disabled:cursor-not-allowed"
                  />
                </div>

                <div class="md:col-span-2 pt-2">
                  <label class="flex items-center gap-3 cursor-pointer">
                    <input
                      type="checkbox"
                      disabled={!isEditing}
                      bind:checked={isGraduated}
                      class="h-4 w-4 rounded-none text-[#1446A0] focus:ring-0 border-[#C8D4E8] disabled:cursor-not-allowed"
                    />

                    <span
                      class="text-sm font-sans font-semibold text-[#0D1B3E]"
                    >
                      Have you already graduated?
                    </span>
                  </label>
                </div>

                <div class="md:col-span-2">
                  <label
                    for="graduationYear"
                    class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                  >
                    {isGraduated
                      ? "Year Graduated *"
                      : "Expected Graduation Year *"}
                  </label>

                  <select
                    id="graduationYear"
                    required
                    disabled={!isEditing}
                    bind:value={graduationYear}
                    class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-mono disabled:bg-[#F2F5FA] disabled:cursor-not-allowed"
                  >
                    <option value="">Select Year</option>
                    {#each yearOptions as year}
                      <option value={year}>{year}</option>
                    {/each}
                  </select>
                </div>
              </div>
            </div>
          {/if}

          <!-- Form actions -->
          {#if isEditing}
            <div
              class="flex items-center justify-end gap-3 pt-6 border-t border-[#C8D4E8]"
            >
              {#if hasExistingProfile}
                <button
                  type="button"
                  onclick={cancelEditing}
                  disabled={saving}
                  class="px-6 py-2.5 text-xs font-mono uppercase tracking-widest border border-[#C8D4E8] text-[#1446A0] disabled:opacity-50"
                >
                  Cancel
                </button>
              {/if}

              <button
                type="submit"
                disabled={saving}
                class="cape-btn-primary px-6 py-2.5 text-xs font-mono uppercase tracking-widest disabled:opacity-50"
              >
                {saving
                  ? "Saving..."
                  : hasExistingProfile
                    ? "Save Profile Changes"
                    : "Complete Profile"}
              </button>
            </div>
          {/if}
        </form>
      </div>
    {/if}
  </div>
</RoleGuard>

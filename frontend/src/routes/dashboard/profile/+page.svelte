<script>
  import RoleGuard from "$lib/components/RoleGuard.svelte";
  import ApiError from "$lib/components/ApiError.svelte";
  import { api } from "$lib/api.js";
  import { currentUser } from "$lib/stores/auth.js";
  import { toast } from "$lib/stores/toast.js";
  import { onMount } from "svelte";
  import { goto } from "$app/navigation";
  import { getRoleDashboard } from "$lib/stores/auth.js";

  const user = $derived($currentUser);
  const userRole = $derived(user?.role?.toUpperCase() || "");

  // Shared & Role-Specific Form States
  let loading = $state(true);
  let saving = $state(false);
  let errorMessage = $state(null);
  let successMessage = $state(null);

  // Read-only user info
  let fullName = $state("");
  let email = $state("");
  let registrationNumber = $state("");

  // SME Fields
  let enterpriseNumber = $state("");
  let registrationDate = $state("");
  let enterpriseType = $state("");
  let enterpriseStatus = $state("");
  let financialYearEnd = $state("");

  // Student / Youth Fields
  let idNumber = $state("");
  let educationalInstitution = $state("");
  let primaryFieldOfStudy = $state("");
  let cellphoneNumber = $state("");
  let isGraduated = $state(false);
  let graduationYear = $state("");

  // Dropdowns
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

  const currentYear = 2026;
  const graduatedYears = Array.from(
    { length: currentYear - 2000 + 1 },
    (_, i) => String(currentYear - i),
  );
  const expectedYears = Array.from({ length: 10 }, (_, i) =>
    String(currentYear + i),
  );
  const yearOptions = $derived(isGraduated ? graduatedYears : expectedYears);

  async function loadProfile() {
    loading = true;
    errorMessage = null;
    try {
      const profile = (await api.profile?.get?.()) || {};

      // Populate Read-only user details
      fullName = user?.name || profile.fullName || "";
      email = user?.email || profile.email || "";
      registrationNumber =
        user?.registrationNumber || profile.registrationNumber || "";

      registrationDate =
        profile.registrationDate || user?.registrationDate || "";

      // Populate Student fields
      idNumber = profile.idNumber || user?.idNumber || "";
      educationalInstitution =
        profile.educationalInstitution || user?.educationalInstitution || "";
      primaryFieldOfStudy =
        profile.primaryFieldOfStudy || user?.primaryFieldOfStudy || "";
      cellphoneNumber = profile.cellphoneNumber || user?.cellphoneNumber || "";
      isGraduated = profile.isGraduated ?? user?.isGraduated ?? false;
      graduationYear = profile.graduationYear || user?.graduationYear || "";
    } catch (err) {
      console.error("Failed to load profile:", err);
    } finally {
      loading = false;
    }
  }

  async function handleSaveProfile(event) {
    event?.preventDefault();
    saving = true;
    errorMessage = null;
    successMessage = null;

    if (userRole === "YOUTH" || userRole === "ADMIN") {
      const idDigitsOnly = /^\d{13}$/;
      if (!idDigitsOnly.test(idNumber)) {
        errorMessage =
          "South African ID Number must be exactly 13 digits long and contain numbers only.";
        saving = false;
        return;
      }

      if (userRole === "SME" || userRole === "ADMIN") {
        const regFormat = /^\d{4}\/\d{6}\/\d{2}$/;
        if (!regFormat.test(registrationNumber)) {
          errorMessage =
            "Registration number must follow the strict format: 4 digits / 6 digits / 2 digits (e.g. 2021/123456/07).";
          saving = false;
          return;
        }
      }

      const phoneDigitsOnly = /^\d{10}$/;
      if (!phoneDigitsOnly.test(cellphoneNumber)) {
        errorMessage =
          "Cellphone number must be exactly 10 digits long and contain numbers only.";
        saving = false;
        return;
      }
    }

    const payload =
      userRole === "SME"
        ? {
            registrationNumber: registrationNumber.trim(),
            registrationDate,
            enterpriseType,
            enterpriseStatus,
            financialYearEnd,
          }
        : {
            idNumber: idNumber.trim(),
            educationalInstitution: educationalInstitution.trim(),
            primaryFieldOfStudy: primaryFieldOfStudy.trim(),
            cellphoneNumber: cellphoneNumber.trim(),
            isGraduated,
            graduationYear,
          };

    try {
      if (api.profile?.update) {
        await api.profile.update(payload);
      }
      toast.success("Profile updated successfully!");
      successMessage = "Your profile details have been saved.";

      const targetDashboard = getRoleDashboard
        ? getRoleDashboard(user?.role)
        : "/dashboard/youth";
      goto(targetDashboard);
    } catch (err) {
      errorMessage = err.message || "Failed to update profile.";
      toast.error("Save failed.");
    } finally {
      saving = false;
    }
  }

  onMount(() => {
    loadProfile();
  });
</script>

<svelte:head>
  <title>Complete Your Profile | Shared Services Hub</title>
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
        <span
          >{userRole === "SME"
            ? "SME COMPLIANCE LEDGER"
            : "YOUTH EXPERIENCE LEDGER"}</span
        >
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
    {:else}
      <div class="border-2 border-[#1446A0] bg-white p-6 sm:p-8 shadow-md">
        <form onsubmit={handleSaveProfile} class="space-y-6">
          <!-- SHARED: Read-Only Full Name & Email -->
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
                type="text"
                disabled
                value={email}
                class="mt-1.5 block w-full px-4 py-2.5 text-sm font-mono bg-[#F2F5FA] border border-[#C8D4E8] text-[#6A84AC] cursor-not-allowed"
              />
            </div>
          </div>

          <!-- CONDITIONAL SECTION: SME PROFILE FIELDS -->
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
                  Registration Number (Locked) *
                </label>
                <input
                  id="registrationNumber"
                  type="text"
                  disabled
                  value={registrationNumber}
                  class="mt-1.5 block w-full px-4 py-2.5 text-sm font-mono bg-[#F2F5FA] border border-[#C8D4E8] text-[#6A84AC] cursor-not-allowed"
                />
                <span class="text-[10px] text-[#6A84AC] font-mono mt-1 block"
                ></span>
              </div>
              <div>
                <label
                  for="enterpriseType"
                  class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                  >Enterprise Type *</label
                >
                <select
                  id="enterpriseType"
                  bind:value={enterpriseType}
                  class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm"
                >
                  {#each enterpriseTypes as t}
                    <option value={t}>{t}</option>
                  {/each}
                </select>
              </div>
              <div>
                <label
                  for="enterpriseStatus"
                  class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                  >Enterprise Status *</label
                >
                <select
                  id="enterpriseStatus"
                  bind:value={enterpriseStatus}
                  class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm"
                >
                  {#each enterpriseStatuses as s}
                    <option value={s}>{s}</option>
                  {/each}
                </select>
              </div>
              <div class="md:col-span-2">
                <label
                  for="financialYearEnd"
                  class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                  >Financial Year End *</label
                >
                <select
                  id="financialYearEnd"
                  bind:value={financialYearEnd}
                  class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-mono"
                >
                  {#each financialYearEnds as m}
                    <option value={m}>{m}</option>
                  {/each}
                </select>
              </div>
            </div>
          {/if}

          <!-- CONDITIONAL SECTION: STUDENT / YOUTH PROFILE FIELDS -->
          {#if userRole === "YOUTH" || userRole === "ADMIN"}
            <div class="space-y-6 pt-4">
              <h3
                class="font-serif text-xl text-[#0D1B3E] border-b border-[#C8D4E8] pb-2"
              >
                Student & Academic Credentials
              </h3>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <!-- ID Number (Strictly 13 digits) -->
                <div>
                  <label
                    for="idNumber"
                    class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                    >ID Number (13 Digits) *</label
                  >
                  <input
                    id="idNumber"
                    type="text"
                    maxlength="13"
                    required
                    bind:value={idNumber}
                    placeholder="e.g. 0101015009088"
                    class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-mono"
                  />
                  <span class="text-[10px] text-[#6A84AC] font-mono mt-1 block"
                    >Must be exactly 13 digits</span
                  >
                </div>

                <!-- Cellphone Number (Strictly 10 digits) -->
                <div>
                  <label
                    for="cellphoneNumber"
                    class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                    >Cellphone Number (10 Digits) *</label
                  >
                  <input
                    id="cellphoneNumber"
                    type="tel"
                    maxlength="10"
                    required
                    bind:value={cellphoneNumber}
                    placeholder="e.g. 0821234567"
                    class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-mono"
                  />
                  <span class="text-[10px] text-[#6A84AC] font-mono mt-1 block"
                    >Must be exactly 10 digits</span
                  >
                </div>

                <!-- Educational Institution -->
                <div>
                  <label
                    for="educationalInstitution"
                    class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                    >Educational Institution *</label
                  >
                  <input
                    id="educationalInstitution"
                    type="text"
                    required
                    bind:value={educationalInstitution}
                    placeholder="e.g. University of Cape Town / TETA"
                    class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-mono"
                  />
                </div>

                <!-- Primary Field of Study -->
                <div>
                  <label
                    for="primaryFieldOfStudy"
                    class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]"
                    >Primary Field of Study *</label
                  >
                  <input
                    id="primaryFieldOfStudy"
                    type="text"
                    required
                    bind:value={primaryFieldOfStudy}
                    placeholder="e.g. Information Technology / Digital Operations"
                    class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-mono"
                  />
                </div>

                <!-- Graduation Checkbox -->
                <div class="md:col-span-2 pt-2">
                  <label class="flex items-center gap-3 cursor-pointer">
                    <input
                      type="checkbox"
                      bind:checked={isGraduated}
                      class="h-4 w-4 rounded-none text-[#1446A0] focus:ring-0 border-[#C8D4E8]"
                    />
                    <span
                      class="text-sm font-sans font-semibold text-[#0D1B3E]"
                    >
                      Have you already graduated?
                    </span>
                  </label>
                </div>

                <!-- Conditional Graduation Year / Expected Graduation Year Dropdown -->
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
                    bind:value={graduationYear}
                    required
                    class="cape-input mt-1.5 block w-full px-4 py-2.5 text-sm font-mono"
                  >
                    <option value="" disabled>Select Year</option>
                    {#each yearOptions as yr}
                      <option value={yr}>{yr}</option>
                    {/each}
                  </select>
                </div>
              </div>
            </div>
          {/if}

          <!-- Submit Button -->
          <div
            class="flex items-center justify-end gap-3 pt-6 border-t border-[#C8D4E8]"
          >
            <button
              type="submit"
              disabled={saving}
              class="cape-btn-primary px-6 py-2.5 text-xs font-mono uppercase tracking-widest disabled:opacity-50"
            >
              {saving ? "Saving..." : "Save Profile Changes"}
            </button>
          </div>
        </form>
      </div>
    {/if}
  </div>
</RoleGuard>

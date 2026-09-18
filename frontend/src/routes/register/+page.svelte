<script>
	import { api } from '$lib/api.js';
	import { auth } from '$lib/stores/auth.js';
	import { toast } from '$lib/stores/toast.js';
	import ApiError from '$lib/components/ApiError.svelte';

	let name = $state('');
	let email = $state('');
	let password = $state('');
	let role = $state('YOUTH');

	// SME-only fields
	let businessName = $state('');
	let registrationNumber = $state('');
	let industry = $state('Retail & E-commerce');

	let loading = $state(false);
	let errorMessage = $state(null);

	const isSme = $derived(role === 'SME');

	async function handleSubmit(event) {
		event?.preventDefault();
		errorMessage = null;

		if (!name.trim()) {
			errorMessage = 'Please enter your full name.';
			return;
		}
		if (!email.trim() || !email.includes('@')) {
			errorMessage = 'Please provide a valid email address.';
			return;
		}
		if (password.length < 8) {
			errorMessage = 'Password must be at least 8 characters long.';
			return;
		}

		if (isSme && !businessName.trim()) {
			errorMessage = 'Please provide your registered SME business name.';
			return;
		}

		loading = true;

		const payload = {
			name: name.trim(),
			email: email.trim(),
			password,
			role,
			...(isSme
				? {
						businessName: businessName.trim(),
						registrationNumber: registrationNumber.trim(),
						industry: industry.trim()
					}
				: {})
		};

		try {
			const res = await api.auth.register(payload);
			toast.success('Registration successful! Welcome to the hub.');
			auth.login(res, true);
		} catch (err) {
			errorMessage = err.message || 'Registration failed. Please check your information.';
		} finally {
			loading = false;
		}
	}
</script>

<svelte:head>
	<title>Register | Shared Services Hub</title>
</svelte:head>

<div class="min-h-[85vh] flex flex-col justify-center py-12 sm:px-6 lg:px-8 bg-[#F8F9FC]">
	<div class="sm:mx-auto sm:w-full sm:max-w-xl text-center">
		<div class="inline-flex h-11 w-11 items-center justify-center bg-[#1446A0] text-white font-mono font-bold text-base shadow-xs mb-4">
			SSH
		</div>
		<h2 class="font-serif text-3xl sm:text-4xl text-[#0D1B3E] font-medium tracking-tight">
			Create Hub Account
		</h2>
		<p class="mt-2 text-sm text-[#6A84AC] font-sans">
			Already have an account?
			<a href="/login" class="font-semibold text-[#1446A0] hover:underline">
				Sign in here
			</a>
		</p>
	</div>

	<div class="mt-8 sm:mx-auto sm:w-full sm:max-w-xl px-4 sm:px-0">
		<div class="bg-white py-8 px-6 sm:px-10 border border-[#C8D4E8] shadow-md">
			<!-- Error Banner -->
			<ApiError message={errorMessage} ondismiss={() => (errorMessage = null)} />

			<form class="space-y-5" onsubmit={handleSubmit}>
				<!-- Role Selection -->
				<div>
					<label for="role" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
						Select Profile Type
					</label>
					<div class="mt-2 grid grid-cols-3 gap-2">
						<button
							type="button"
							class="border py-2.5 px-3 text-xs font-mono font-bold transition-all text-center {role === 'YOUTH' ? 'border-[#1446A0] bg-[#EFF4FF] text-[#1446A0]' : 'border-[#C8D4E8] bg-[#F8F9FC] text-[#0D1B3E] hover:bg-white'}"
							onclick={() => (role = 'YOUTH')}
						>
							YOUTH
						</button>

						<button
							type="button"
							class="border py-2.5 px-3 text-xs font-mono font-bold transition-all text-center {role === 'SME' ? 'border-[#1446A0] bg-[#EFF4FF] text-[#1446A0]' : 'border-[#C8D4E8] bg-[#F8F9FC] text-[#0D1B3E] hover:bg-white'}"
							onclick={() => (role = 'SME')}
						>
							SME PARTNER
						</button>

						<button
							type="button"
							class="border py-2.5 px-3 text-xs font-mono font-bold transition-all text-center {role === 'CORPORATE' ? 'border-[#1446A0] bg-[#EFF4FF] text-[#1446A0]' : 'border-[#C8D4E8] bg-[#F8F9FC] text-[#0D1B3E] hover:bg-white'}"
							onclick={() => (role = 'CORPORATE')}
						>
							CORPORATE
						</button>
					</div>
				</div>

				<!-- Full Name -->
				<div>
					<label for="name" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
						{isSme ? 'Contact Person Full Name' : 'Full Name'}
					</label>
					<div class="mt-1.5">
						<input
							id="name"
							name="name"
							type="text"
							required
							bind:value={name}
							placeholder="Sipho Ndlovu"
							class="cape-input block w-full px-3.5 py-2.5 text-sm"
						/>
					</div>
				</div>

				<!-- Email Address -->
				<div>
					<label for="email" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
						Email address
					</label>
					<div class="mt-1.5">
						<input
							id="email"
							name="email"
							type="email"
							autocomplete="email"
							required
							bind:value={email}
							placeholder="sipho@example.co.za"
							class="cape-input block w-full px-3.5 py-2.5 text-sm"
						/>
					</div>
				</div>

				<!-- Password -->
				<div>
					<div class="flex items-center justify-between">
						<label for="password" class="block text-xs font-mono font-bold uppercase tracking-wider text-[#0D1B3E]">
							Password
						</label>
						<span class="text-[11px] font-mono text-[#6A84AC]">Min 8 chars</span>
					</div>
					<div class="mt-1.5">
						<input
							id="password"
							name="password"
							type="password"
							autocomplete="new-password"
							required
							minlength="8"
							bind:value={password}
							placeholder="••••••••"
							class="cape-input block w-full px-3.5 py-2.5 text-sm"
						/>
					</div>
				</div>

				<!-- Conditionally Rendered SME Fields -->
				{#if isSme}
					<div class="border-2 border-[#D4831A] bg-[#FFFBF2] p-5 space-y-4">
						<div class="font-mono text-xs font-bold uppercase tracking-wider text-[#D4831A]">
							[ SME Business Registration ]
						</div>

						<div>
							<label for="businessName" class="block text-xs font-mono font-bold text-[#0D1B3E]">
								Registered Business Name *
							</label>
							<input
								id="businessName"
								type="text"
								bind:value={businessName}
								placeholder="Kasi Creative Agency"
								class="cape-input mt-1 block w-full px-3 py-2 text-sm"
							/>
						</div>

						<div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
							<div>
								<label for="regNumber" class="block text-xs font-mono font-bold text-[#0D1B3E]">
									CIPC / Reg Number
								</label>
								<input
									id="regNumber"
									type="text"
									bind:value={registrationNumber}
									placeholder="2023/123456/07"
									class="cape-input mt-1 block w-full px-3 py-2 text-sm"
								/>
							</div>

							<div>
								<label for="industry" class="block text-xs font-mono font-bold text-[#0D1B3E]">
									Industry Sector
								</label>
								<select
									id="industry"
									bind:value={industry}
									class="cape-input mt-1 block w-full px-3 py-2 text-sm"
								>
									<option value="Retail & E-commerce">Retail & E-commerce</option>
									<option value="Marketing & Design">Marketing & Design</option>
									<option value="Information Technology">Information Technology</option>
									<option value="Accounting & Financial">Accounting & Financial</option>
									<option value="Logistics & Transport">Logistics & Transport</option>
									<option value="Hospitality & Catering">Hospitality & Catering</option>
									<option value="Other">Other Services</option>
								</select>
							</div>
						</div>
					</div>
				{/if}

				<!-- Submit Button -->
				<div class="pt-2">
					<button
						type="submit"
						disabled={loading}
						class="cape-btn-primary flex w-full justify-center items-center py-3 text-xs font-mono uppercase tracking-widest disabled:opacity-50"
					>
						{#if loading}
							<span>Creating Profile...</span>
						{:else}
							<span>Complete Registration</span>
						{/if}
					</button>
				</div>
			</form>
		</div>
	</div>
</div>

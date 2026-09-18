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

<div class="min-h-[85vh] flex flex-col justify-center py-12 sm:px-6 lg:px-8">
	<div class="sm:mx-auto sm:w-full sm:max-w-xl text-center">
		<div class="inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-600 text-white font-extrabold text-xl shadow-md shadow-indigo-500/20 mb-3">
			SSH
		</div>
		<h2 class="text-3xl font-extrabold text-slate-900 tracking-tight">
			Join the Shared Services Hub
		</h2>
		<p class="mt-2 text-sm text-slate-600">
			Already have an account?
			<a href="/login" class="font-semibold text-indigo-600 hover:text-indigo-500 hover:underline">
				Sign in here
			</a>
		</p>
	</div>

	<div class="mt-8 sm:mx-auto sm:w-full sm:max-w-xl px-4 sm:px-0">
		<div class="bg-white py-8 px-6 shadow-xl shadow-slate-200/50 sm:rounded-3xl border border-slate-200/80 sm:px-10">
			<!-- Error Banner -->
			<ApiError message={errorMessage} ondismiss={() => (errorMessage = null)} />

			<form class="space-y-5" onsubmit={handleSubmit}>
				<!-- Role Selection -->
				<div>
					<label for="role" class="block text-sm font-semibold text-slate-700">
						I am registering as:
					</label>
					<div class="mt-1.5 grid grid-cols-3 gap-2">
						<button
							type="button"
							class="rounded-xl border py-2.5 px-3 text-xs font-bold transition-all text-center {role === 'YOUTH' ? 'border-indigo-600 bg-indigo-50/80 text-indigo-700 ring-2 ring-indigo-600/20' : 'border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100'}"
							onclick={() => (role = 'YOUTH')}
						>
							Youth Candidate
						</button>

						<button
							type="button"
							class="rounded-xl border py-2.5 px-3 text-xs font-bold transition-all text-center {role === 'SME' ? 'border-indigo-600 bg-indigo-50/80 text-indigo-700 ring-2 ring-indigo-600/20' : 'border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100'}"
							onclick={() => (role = 'SME')}
						>
							SME Business
						</button>

						<button
							type="button"
							class="rounded-xl border py-2.5 px-3 text-xs font-bold transition-all text-center {role === 'CORPORATE' ? 'border-indigo-600 bg-indigo-50/80 text-indigo-700 ring-2 ring-indigo-600/20' : 'border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100'}"
							onclick={() => (role = 'CORPORATE')}
						>
							Corporate Sponsor
						</button>
					</div>
				</div>

				<!-- Full Name -->
				<div>
					<label for="name" class="block text-sm font-semibold text-slate-700">
						{isSme ? 'Contact Person Full Name' : 'Full Name'}
					</label>
					<div class="mt-1">
						<input
							id="name"
							name="name"
							type="text"
							required
							bind:value={name}
							placeholder="e.g. Sipho Ndlovu"
							class="block w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-slate-900 placeholder:text-slate-400 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20 text-sm shadow-2xs"
						/>
					</div>
				</div>

				<!-- Email Address -->
				<div>
					<label for="email" class="block text-sm font-semibold text-slate-700">
						Email address
					</label>
					<div class="mt-1">
						<input
							id="email"
							name="email"
							type="email"
							autocomplete="email"
							required
							bind:value={email}
							placeholder="e.g. sipho@example.co.za"
							class="block w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-slate-900 placeholder:text-slate-400 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20 text-sm shadow-2xs"
						/>
					</div>
				</div>

				<!-- Password -->
				<div>
					<div class="flex items-center justify-between">
						<label for="password" class="block text-sm font-semibold text-slate-700">
							Password
						</label>
						<span class="text-xs text-slate-500 font-medium">At least 8 characters</span>
					</div>
					<div class="mt-1">
						<input
							id="password"
							name="password"
							type="password"
							autocomplete="new-password"
							required
							minlength="8"
							bind:value={password}
							placeholder="••••••••"
							class="block w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-slate-900 placeholder:text-slate-400 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20 text-sm shadow-2xs"
						/>
					</div>
				</div>

				<!-- Conditionally Rendered SME Fields -->
				{#if isSme}
					<div class="rounded-2xl border border-amber-200 bg-amber-50/50 p-5 space-y-4 animate-in fade-in slide-in-from-top-2 duration-150">
						<div class="flex items-center gap-2 text-amber-800 text-xs font-bold uppercase tracking-wider">
							<svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
							</svg>
							<span>SME Business Profile</span>
						</div>

						<div>
							<label for="businessName" class="block text-xs font-semibold text-slate-700">
								Registered Business Name
							</label>
							<input
								id="businessName"
								type="text"
								bind:value={businessName}
								placeholder="e.g. Kasi Creative Agency"
								class="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 text-sm focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20"
							/>
						</div>

						<div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
							<div>
								<label for="regNumber" class="block text-xs font-semibold text-slate-700">
									CIPC / Reg Number (Optional)
								</label>
								<input
									id="regNumber"
									type="text"
									bind:value={registrationNumber}
									placeholder="e.g. 2023/123456/07"
									class="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 text-sm focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20"
								/>
							</div>

							<div>
								<label for="industry" class="block text-xs font-semibold text-slate-700">
									Industry
								</label>
								<select
									id="industry"
									bind:value={industry}
									class="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 text-sm focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20"
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
						class="flex w-full justify-center items-center rounded-xl bg-indigo-600 px-4 py-3 text-sm font-bold text-white shadow-md shadow-indigo-500/20 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 transition-all disabled:opacity-50"
					>
						{#if loading}
							<svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
								<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
								<path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
							</svg>
							<span>Creating account...</span>
						{:else}
							<span>Create Account</span>
						{/if}
					</button>
				</div>
			</form>
		</div>
	</div>
</div>

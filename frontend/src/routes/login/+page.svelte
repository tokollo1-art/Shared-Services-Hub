<script>
	import { api } from '$lib/api.js';
	import { auth, isAuthenticated, currentUser, getRoleDashboard } from '$lib/stores/auth.js';
	import { toast } from '$lib/stores/toast.js';
	import ApiError from '$lib/components/ApiError.svelte';
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';

	let email = $state('');
	let password = $state('');
	let loading = $state(false);
	let errorMessage = $state(null);

	onMount(() => {
		if ($isAuthenticated && $currentUser?.role) {
			goto(getRoleDashboard($currentUser.role));
		}
	});

	async function handleSubmit(event) {
		event?.preventDefault();
		if (!email || !password) {
			errorMessage = 'Please enter both email and password.';
			return;
		}

		loading = true;
		errorMessage = null;

		try {
			const res = await api.auth.login({ email, password });
			toast.success('Logged in successfully!');
			auth.login(res, true);
		} catch (err) {
			errorMessage = err.message || 'Login failed. Please verify your credentials.';
		} finally {
			loading = false;
		}
	}

	function fillDemoCredentials(demoRole) {
		errorMessage = null;
		if (demoRole === 'YOUTH') {
			email = 'thabo@example.com';
			password = 'password123';
		} else if (demoRole === 'SME') {
			email = 'sme@craftworks.co.za';
			password = 'password123';
		} else if (demoRole === 'CORPORATE') {
			email = 'corporate@standardbank.co.za';
			password = 'password123';
		} else if (demoRole === 'ADMIN') {
			email = 'admin@ssh.org.za';
			password = 'password123';
		}
	}
</script>

<svelte:head>
	<title>Sign In | Shared Services Hub</title>
</svelte:head>

<div class="min-h-[80vh] flex flex-col justify-center py-12 sm:px-6 lg:px-8 bg-[#F8F9FC]">
	<div class="sm:mx-auto sm:w-full sm:max-w-md text-center">
		<div class="inline-flex h-11 w-11 items-center justify-center bg-[#1446A0] text-white font-mono font-bold text-base shadow-xs mb-4">
			SSH
		</div>
		<h2 class="font-serif text-3xl sm:text-4xl text-[#0D1B3E] font-medium tracking-tight">
			Sign In to Account
		</h2>
		<p class="mt-2 text-sm text-[#6A84AC] font-sans">
			Or
			<a href="/register" class="font-semibold text-[#1446A0] hover:underline">
				create a new candidate or business profile
			</a>
		</p>
	</div>

	<div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
		<div class="bg-white py-8 px-6 sm:px-10 border border-[#C8D4E8] shadow-md">
			<!-- Error Banner -->
			<ApiError message={errorMessage} ondismiss={() => (errorMessage = null)} />

			<form class="space-y-5" onsubmit={handleSubmit}>
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
							placeholder="candidate@example.co.za"
							class="cape-input block w-full px-3.5 py-2.5 text-sm"
						/>
					</div>
				</div>

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
							autocomplete="current-password"
							required
							bind:value={password}
							placeholder="••••••••"
							class="cape-input block w-full px-3.5 py-2.5 text-sm"
						/>
					</div>
				</div>

				<div>
					<button
						type="submit"
						disabled={loading}
						class="cape-btn-primary flex w-full justify-center items-center py-3 text-xs font-mono uppercase tracking-widest disabled:opacity-50"
					>
						{#if loading}
							<span>Signing in...</span>
						{:else}
							<span>Authenticate Account</span>
						{/if}
					</button>
				</div>
			</form>

			<!-- Quick Demo Selector (Sharp, Editorial) -->
			<div class="mt-8 pt-6 border-t border-[#C8D4E8]">
				<p class="font-mono text-[10px] font-bold uppercase tracking-widest text-[#6A84AC] text-center mb-3">
					Quick Account Switcher
				</p>
				<div class="grid grid-cols-2 gap-2">
					<button
						type="button"
						class="border border-[#C8D4E8] bg-[#F8F9FC] p-2.5 text-left hover:border-[#1446A0] hover:bg-white transition-colors"
						onclick={() => fillDemoCredentials('YOUTH')}
					>
						<span class="block font-mono text-[11px] font-bold text-[#1446A0]">YOUTH</span>
						<span class="text-[11px] text-[#6A84AC]">Thabo Mokoena</span>
					</button>

					<button
						type="button"
						class="border border-[#C8D4E8] bg-[#F8F9FC] p-2.5 text-left hover:border-[#D4831A] hover:bg-white transition-colors"
						onclick={() => fillDemoCredentials('SME')}
					>
						<span class="block font-mono text-[11px] font-bold text-[#D4831A]">SME</span>
						<span class="text-[11px] text-[#6A84AC]">Kasi Craftworks</span>
					</button>

					<button
						type="button"
						class="border border-[#C8D4E8] bg-[#F8F9FC] p-2.5 text-left hover:border-[#1A8A55] hover:bg-white transition-colors"
						onclick={() => fillDemoCredentials('CORPORATE')}
					>
						<span class="block font-mono text-[11px] font-bold text-[#1A8A55]">CORPORATE</span>
						<span class="text-[11px] text-[#6A84AC]">Standard Bank</span>
					</button>

					<button
						type="button"
						class="border border-[#C8D4E8] bg-[#F8F9FC] p-2.5 text-left hover:border-[#7C3FE4] hover:bg-white transition-colors"
						onclick={() => fillDemoCredentials('ADMIN')}
					>
						<span class="block font-mono text-[11px] font-bold text-[#7C3FE4]">ADMIN</span>
						<span class="text-[11px] text-[#6A84AC]">Platform Admin</span>
					</button>
				</div>
			</div>
		</div>
	</div>
</div>

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

<div class="min-h-[80vh] flex flex-col justify-center py-12 sm:px-6 lg:px-8">
	<div class="sm:mx-auto sm:w-full sm:max-w-md text-center">
		<div class="inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-600 text-white font-extrabold text-xl shadow-md shadow-indigo-500/20 mb-3">
			SSH
		</div>
		<h2 class="text-3xl font-extrabold text-slate-900 tracking-tight">
			Sign in to your account
		</h2>
		<p class="mt-2 text-sm text-slate-600">
			Or
			<a href="/register" class="font-semibold text-indigo-600 hover:text-indigo-500 hover:underline">
				create a new candidate or business profile
			</a>
		</p>
	</div>

	<div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
		<div class="bg-white py-8 px-6 shadow-xl shadow-slate-200/50 sm:rounded-3xl border border-slate-200/80 sm:px-10">
			<!-- Error Banner -->
			<ApiError message={errorMessage} ondismiss={() => (errorMessage = null)} />

			<form class="space-y-5" onsubmit={handleSubmit}>
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
							placeholder="e.g. candidate@example.co.za"
							class="block w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-slate-900 placeholder:text-slate-400 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20 text-sm shadow-2xs"
						/>
					</div>
				</div>

				<div>
					<div class="flex items-center justify-between">
						<label for="password" class="block text-sm font-semibold text-slate-700">
							Password
						</label>
						<span class="text-xs text-slate-400">Min 8 characters</span>
					</div>
					<div class="mt-1">
						<input
							id="password"
							name="password"
							type="password"
							autocomplete="current-password"
							required
							bind:value={password}
							placeholder="••••••••"
							class="block w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-slate-900 placeholder:text-slate-400 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20 text-sm shadow-2xs"
						/>
					</div>
				</div>

				<div>
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
							<span>Signing in...</span>
						{:else}
							<span>Sign in</span>
						{/if}
					</button>
				</div>
			</form>

			<!-- Quick Demo Selector -->
			<div class="mt-8 pt-6 border-t border-slate-100">
				<p class="text-xs font-bold uppercase tracking-wider text-slate-400 text-center mb-3">
					Quick Demo Login Switcher
				</p>
				<div class="grid grid-cols-2 gap-2">
					<button
						type="button"
						class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-xs font-semibold text-slate-700 hover:bg-indigo-50 hover:border-indigo-200 hover:text-indigo-700 transition-colors text-left"
						onclick={() => fillDemoCredentials('YOUTH')}
					>
						<span class="block text-indigo-600 font-bold">Youth Alumni</span>
						<span class="text-[11px] text-slate-500">Thabo Mokoena</span>
					</button>

					<button
						type="button"
						class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-xs font-semibold text-slate-700 hover:bg-indigo-50 hover:border-indigo-200 hover:text-indigo-700 transition-colors text-left"
						onclick={() => fillDemoCredentials('SME')}
					>
						<span class="block text-amber-700 font-bold">SME Partner</span>
						<span class="text-[11px] text-slate-500">Kasi Craftworks</span>
					</button>

					<button
						type="button"
						class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-xs font-semibold text-slate-700 hover:bg-indigo-50 hover:border-indigo-200 hover:text-indigo-700 transition-colors text-left"
						onclick={() => fillDemoCredentials('CORPORATE')}
					>
						<span class="block text-emerald-700 font-bold">Corporate</span>
						<span class="text-[11px] text-slate-500">Standard Bank</span>
					</button>

					<button
						type="button"
						class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-xs font-semibold text-slate-700 hover:bg-indigo-50 hover:border-indigo-200 hover:text-indigo-700 transition-colors text-left"
						onclick={() => fillDemoCredentials('ADMIN')}
					>
						<span class="block text-purple-700 font-bold">Administrator</span>
						<span class="text-[11px] text-slate-500">Platform Admin</span>
					</button>
				</div>
			</div>
		</div>
	</div>
</div>

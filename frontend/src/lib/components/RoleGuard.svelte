<script>
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { auth, getRoleDashboard } from '$lib/stores/auth.js';
	import { onMount } from 'svelte';

	/**
	 * @typedef {Object} Props
	 * @property {string|string[]} [allowedRoles]
	 * @property {boolean} [requireAuth]
	 * @property {import('svelte').Snippet} [children]
	 */

	/** @type {Props} */
	let {
		allowedRoles = [],
		requireAuth = true,
		children
	} = $props();

	let isAuthorized = $state(false);
	let isChecking = $state(true);

	const rolesArray = $derived.by(() => {
		if (!allowedRoles) return [];
		return Array.isArray(allowedRoles) ? allowedRoles.map(r => r.toUpperCase()) : [allowedRoles.toUpperCase()];
	});

	function checkAccess() {
		if (!browser) return;

		// 1. Must be initialized
		if (!$auth.initialized) {
			return;
		}

		// 2. Unauthenticated check
		if (requireAuth && (!$auth.token || !$auth.user)) {
			isAuthorized = false;
			isChecking = false;
			goto('/login');
			return;
		}

		// 3. Role verification
		if (rolesArray.length > 0 && $auth.user?.role) {
			const userRole = $auth.user.role.toUpperCase();
			if (!rolesArray.includes(userRole)) {
				isAuthorized = false;
				isChecking = false;
				const properDashboard = getRoleDashboard(userRole);
				goto(properDashboard);
				return;
			}
		}

		isAuthorized = true;
		isChecking = false;
	}

	$effect(() => {
		if ($auth.initialized) {
			checkAccess();
		}
	});

	onMount(() => {
		auth.init();
	});
</script>

{#if isChecking}
	<div class="min-h-[50vh] flex flex-col items-center justify-center p-8">
		<div class="relative flex items-center justify-center">
			<div class="h-12 w-12 rounded-full border-4 border-indigo-100 border-t-indigo-600 animate-spin"></div>
			<div class="absolute h-6 w-6 rounded-full bg-indigo-50"></div>
		</div>
		<p class="mt-4 text-sm font-medium text-slate-500 animate-pulse">Verifying permissions...</p>
	</div>
{:else if isAuthorized && children}
	{@render children()}
{/if}

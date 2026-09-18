<script>
	import './layout.css';
	import Navbar from '$lib/components/Navbar.svelte';
	import { auth } from '$lib/stores/auth.js';
	import { toast } from '$lib/stores/toast.js';
	import { onMount } from 'svelte';

	let { children } = $props();

	onMount(() => {
		auth.init();
	});

	const toasts = $derived($toast);
</script>

<div class="flex min-h-screen flex-col bg-slate-50">
	<!-- Navbar -->
	<Navbar />

	<!-- Main Content Area -->
	<main class="flex-1">
		{@render children()}
	</main>

	<!-- Global Toast Notifications -->
	<div
		class="fixed bottom-5 right-5 z-50 flex flex-col gap-2.5 max-w-md w-full pointer-events-none px-4 sm:px-0"
		aria-live="polite"
	>
		{#each toasts as t (t.id)}
			<div
				class="pointer-events-auto flex items-start gap-3 rounded-xl border p-4 shadow-xl backdrop-blur-md transition-all animate-in slide-in-from-bottom-5 duration-200 {t.type === 'success'
					? 'border-emerald-200 bg-white/95 text-slate-800'
					: t.type === 'error'
					? 'border-rose-200 bg-white/95 text-slate-800'
					: 'border-indigo-200 bg-white/95 text-slate-800'}"
			>
				<div class="shrink-0 mt-0.5">
					{#if t.type === 'success'}
						<div class="rounded-full bg-emerald-100 p-1 text-emerald-600">
							<svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7" />
							</svg>
						</div>
					{:else if t.type === 'error'}
						<div class="rounded-full bg-rose-100 p-1 text-rose-600">
							<svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
							</svg>
						</div>
					{:else}
						<div class="rounded-full bg-indigo-100 p-1 text-indigo-600">
							<svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
							</svg>
						</div>
					{/if}
				</div>

				<div class="flex-1 text-sm font-medium leading-snug">
					{t.message}
				</div>

				<button
					type="button"
					class="shrink-0 rounded-md p-1 text-slate-400 hover:text-slate-600"
					onclick={() => toast.remove(t.id)}
					aria-label="Dismiss toast"
				>
					<svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
					</svg>
				</button>
			</div>
		{/each}
	</div>

	<!-- Footer -->
	<footer class="border-t border-slate-200 bg-white">
		<div class="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
			<div class="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
				<div class="md:col-span-2">
					<div class="flex items-center gap-2.5">
						<div class="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-600 text-white font-extrabold text-sm">
							SSH
						</div>
						<span class="text-lg font-bold text-slate-900">Shared Services Hub</span>
					</div>
					<p class="mt-3 text-sm text-slate-600 max-w-md leading-relaxed">
						Connecting South African youth program alumni to SMEs for paid tasks. Funded by corporate enterprise development budgets, verified on the permanent Experience Ledger.
					</p>
					<div class="mt-4 flex items-center gap-3 text-xs font-semibold text-slate-500">
						<span class="flex items-center gap-1">
							<span class="h-2 w-2 rounded-full bg-emerald-500"></span>
							B-BBEE Scorecard Compliant
						</span>
						<span>•</span>
						<span>Zero Cost to SMEs</span>
						<span>•</span>
						<span>100% Guaranteed Wages</span>
					</div>
				</div>

				<div>
					<h4 class="text-xs font-bold uppercase tracking-wider text-slate-900 mb-3">Platform</h4>
					<ul class="space-y-2 text-sm text-slate-600">
						<li><a href="/tasks" class="hover:text-indigo-600 transition-colors">Browse Tasks</a></li>
						<li><a href="/register" class="hover:text-indigo-600 transition-colors">Join as Youth</a></li>
						<li><a href="/register" class="hover:text-indigo-600 transition-colors">Register as SME</a></li>
						<li><a href="/register" class="hover:text-indigo-600 transition-colors">Corporate Sponsorship</a></li>
					</ul>
				</div>

				<div>
					<h4 class="text-xs font-bold uppercase tracking-wider text-slate-900 mb-3">The SSH Guarantee</h4>
					<ul class="space-y-2 text-sm text-slate-600">
						<li class="flex items-center gap-1.5 text-emerald-700 font-medium">
							<span>✓ Guaranteed Work</span>
						</li>
						<li class="flex items-center gap-1.5 text-emerald-700 font-medium">
							<span>✓ Guaranteed Direct Pay</span>
						</li>
						<li class="flex items-center gap-1.5 text-emerald-700 font-medium">
							<span>✓ Verifiable Experience</span>
						</li>
					</ul>
				</div>
			</div>

			<div class="border-t border-slate-100 pt-8 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-500 gap-4">
				<p>© 2026 Shared Services Hub (Pty) Ltd. Republic of South Africa. All rights reserved.</p>
				<p class="text-slate-400">Addressing youth unemployment with sustainable market linkages.</p>
			</div>
		</div>
	</footer>
</div>

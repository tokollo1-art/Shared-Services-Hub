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

<div class="flex min-h-screen flex-col bg-[#F8F9FC]">
	<!-- Navbar -->
	<Navbar />

	<!-- Main Content Area -->
	<main class="flex-1">
		{@render children()}
	</main>

	<!-- Global Toast Notifications (Sharp, Editorial) -->
	<div
		class="fixed bottom-5 right-5 z-50 flex flex-col gap-2 max-w-md w-full pointer-events-none px-4 sm:px-0"
		aria-live="polite"
	>
		{#each toasts as t (t.id)}
			<div
				class="pointer-events-auto flex items-start gap-3 border bg-white p-4 shadow-xl transition-all animate-in slide-in-from-bottom-3 duration-150 {t.type === 'success'
					? 'border-l-4 border-l-[#1A8A55] border-[#C8D4E8] text-[#0D1B3E]'
					: t.type === 'error'
					? 'border-l-4 border-l-[#C42B2B] border-[#C8D4E8] text-[#0D1B3E]'
					: 'border-l-4 border-l-[#1446A0] border-[#C8D4E8] text-[#0D1B3E]'}"
			>
				<div class="shrink-0 mt-0.5 font-mono text-xs font-bold">
					{#if t.type === 'success'}
						<span class="text-[#1A8A55]">[OK]</span>
					{:else if t.type === 'error'}
						<span class="text-[#C42B2B]">[ERR]</span>
					{:else}
						<span class="text-[#1446A0]">[INFO]</span>
					{/if}
				</div>

				<div class="flex-1 text-sm font-medium leading-snug">
					{t.message}
				</div>

				<button
					type="button"
					class="shrink-0 p-1 text-[#6A84AC] hover:text-[#0D1B3E]"
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

	<!-- Footer (Cape Cobalt Surface-Dark #0A1E4A) -->
	<footer class="border-t border-[#C8D4E8] bg-[#0A1E4A] text-white">
		<div class="mx-auto max-w-7xl px-4 py-14 sm:px-6 lg:px-8">
			<div class="grid grid-cols-1 md:grid-cols-4 gap-10 mb-10">
				<div class="md:col-span-2">
					<div class="flex items-center gap-3">
						<div class="flex h-8 w-8 items-center justify-center bg-[#1446A0] text-white font-mono font-bold text-xs border border-white/20">
							SSH
						</div>
						<span class="font-serif text-xl font-bold tracking-tight text-white">Shared Services Hub</span>
					</div>
					<p class="mt-3.5 text-sm text-[#C8D4E8] max-w-md leading-relaxed font-sans">
						Connecting South African youth program alumni to SMEs for paid tasks. Funded by corporate enterprise development reserves, verified through an immutable Experience Ledger.
					</p>
					<div class="mt-5 flex items-center gap-3 text-xs font-mono text-[#6A84AC]">
						<span class="text-[#D4831A] font-semibold">B-BBEE Scorecard Aligned</span>
						<span>•</span>
						<span>R0 Cost to SMEs</span>
						<span>•</span>
						<span class="text-[#1A8A55] font-semibold">100% Guaranteed Wages</span>
					</div>
				</div>

				<div>
					<h4 class="font-mono text-xs font-bold uppercase tracking-widest text-[#D4831A] mb-4">Platform</h4>
					<ul class="space-y-2.5 text-sm text-[#C8D4E8]">
						<li><a href="/tasks" class="hover:text-white hover:underline transition-colors">Task Directory</a></li>
						<li><a href="/register" class="hover:text-white hover:underline transition-colors">Candidate Registration</a></li>
						<li><a href="/register" class="hover:text-white hover:underline transition-colors">SME Partner Access</a></li>
						<li><a href="/register" class="hover:text-white hover:underline transition-colors">Corporate Sponsorship</a></li>
					</ul>
				</div>

				<div>
					<h4 class="font-mono text-xs font-bold uppercase tracking-widest text-[#D4831A] mb-4">The SSH Guarantee</h4>
					<ul class="space-y-2.5 text-sm text-[#C8D4E8]">
						<li class="flex items-center gap-2">
							<span class="text-[#1A8A55] font-mono">✓</span>
							<span>Guaranteed Work Allocation</span>
						</li>
						<li class="flex items-center gap-2">
							<span class="text-[#1A8A55] font-mono">✓</span>
							<span>Direct Corporate Payout</span>
						</li>
						<li class="flex items-center gap-2">
							<span class="text-[#1A8A55] font-mono">✓</span>
							<span>Portable Verified Ledger</span>
						</li>
					</ul>
				</div>
			</div>

			<div class="border-t border-[#1C3565] pt-8 flex flex-col sm:flex-row items-center justify-between text-xs font-mono text-[#6A84AC] gap-4">
				<p>© 2026 Shared Services Hub (Pty) Ltd. Republic of South Africa.</p>
				<p>Palette: Cape Cobalt | Typography: DM Serif &amp; Inter</p>
			</div>
		</div>
	</footer>
</div>
